#!/usr/bin/env python3
"""Exercise production IPC against an isolated native Windows named-pipe server."""
import ctypes, hashlib, json, os, platform, struct, subprocess, threading, time, uuid
from pathlib import Path
from urllib.request import urlopen
ROOT=Path(__file__).resolve().parent.parent

def main():
    if platform.system() != 'Windows':
        print('SKIP native Windows named pipe: requires Windows')
        return
    from ctypes import wintypes as W
    kernel=ctypes.WinDLL('kernel32',use_last_error=True)
    kernel.CreateNamedPipeW.argtypes=[W.LPCWSTR,W.DWORD,W.DWORD,W.DWORD,W.DWORD,W.DWORD,W.DWORD,ctypes.c_void_p]
    kernel.CreateNamedPipeW.restype=W.HANDLE
    kernel.ConnectNamedPipe.argtypes=[W.HANDLE,ctypes.c_void_p]
    kernel.ReadFile.argtypes=[W.HANDLE,ctypes.c_void_p,W.DWORD,ctypes.POINTER(W.DWORD),ctypes.c_void_p]
    kernel.WriteFile.argtypes=kernel.ReadFile.argtypes
    kernel.CloseHandle.argtypes=[W.HANDLE]
    kernel.DisconnectNamedPipe.argtypes=[W.HANDLE]
    work=ROOT/'build/windows-discord-test';work.mkdir(parents=True,exist_ok=True)
    gson=work/'gson-2.10.jar'
    digest='0cdd163ce3598a20fc04eee71b140b24f6f2a3b35f0a499dbbdd9852e83fbfaf'
    if not gson.exists():
        with urlopen('https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10/gson-2.10.jar',timeout=30) as r:gson.write_bytes(r.read())
    assert hashlib.sha256(gson.read_bytes()).hexdigest()==digest,'Gson checksum'
    java=Path(os.environ['JAVA_HOME'])/'bin/java.exe'
    javac=java.with_name('javac.exe')
    sources=[ROOT/'src/com/videomenu'/f'{name}.java' for name in ('WindowsDiscordPipe','EvatexDiscordPresence','DiscordActivityCoordinator')]
    sources.append(ROOT/'tests/WindowsDiscordPresenceRegression.java')
    subprocess.run([str(javac),'-encoding','UTF-8','--release','17','-cp',str(gson),'-d',str(work),*map(str,sources)],check=True)
    name=r'\\.\pipe\evatex-regression-'+uuid.uuid4().hex
    ready=threading.Event();errors=[]
    def peer():
        try:
            for round_no in (1,2):
                handle=kernel.CreateNamedPipeW(name,3,0,1,65536,65536,0,None)
                assert handle!=ctypes.c_void_p(-1).value,ctypes.WinError(ctypes.get_last_error())
                try:
                    ready.set()
                    if not kernel.ConnectNamedPipe(handle,None):assert ctypes.get_last_error()==535,ctypes.WinError(ctypes.get_last_error())
                    def read(n):
                        data=b''
                        while len(data)<n:
                            buf=ctypes.create_string_buffer(n-len(data));count=W.DWORD()
                            if not kernel.ReadFile(handle,buf,len(buf),ctypes.byref(count),None):raise ctypes.WinError(ctypes.get_last_error())
                            assert count.value,'Unexpected EOF'
                            data+=buf.raw[:count.value]
                        return data
                    def receive():
                        op,n=struct.unpack('<II',read(8));assert n<=65536
                        return op,read(n)
                    def send(op,body):
                        data=body.encode();packet=struct.pack('<II',op,len(data))+data
                        # Split the header to catch readNBytes/partial packet mistakes.
                        for fragment in (packet[:3],packet[3:]):
                            count=W.DWORD();buf=ctypes.create_string_buffer(fragment)
                            if not kernel.WriteFile(handle,buf,len(fragment),ctypes.byref(count),None):raise ctypes.WinError(ctypes.get_last_error())
                            assert count.value==len(fragment)
                            time.sleep(.02)
                    op,body=receive();assert op==0 and json.loads(body)['client_id']=='123456789012345678'
                    send(1,'{"evt":"READY","cmd":"DISPATCH"}')
                    # Give the client reader time to wait on an empty pipe before
                    # SET_ACTIVITY is sent from its other thread (the old deadlock).
                    op,body=receive();message=json.loads(body)
                    assert op==1 and message['cmd']=='SET_ACTIVITY'
                    assert message['args']['activity']['details']=='Using EVATEX Background'
                    send(3,'ping')
                    data=dict(message['args']['activity'],details=f'round-{round_no}')
                    send(1,json.dumps(dict(cmd='SET_ACTIVITY',nonce=message['nonce'],evt=None,data=data)))
                    cleared=pong=False
                    while not(cleared and pong):
                        op,body=receive()
                        if op==4:assert body==b'ping';pong=True
                        else:
                            msg=json.loads(body);assert op==1 and msg['cmd']=='SET_ACTIVITY'
                            assert msg['args']['activity'] is None;cleared=True
                finally:
                    kernel.DisconnectNamedPipe(handle);kernel.CloseHandle(handle)
        except BaseException as error:errors.append(error);ready.set()
    thread=threading.Thread(target=peer,daemon=True);thread.start()
    assert ready.wait(5),'Pipe server did not start'
    subprocess.run([str(java),'-cp',str(work)+os.pathsep+str(gson),'com.videomenu.WindowsDiscordPresenceRegression',name],check=True,timeout=40)
    thread.join(5)
    if errors:raise errors[0]
    assert not thread.is_alive(),'Pipe server did not finish'
    print('PASS actual Win32 byte-mode pipe; no live Discord account or socket used',flush=True)

if __name__=='__main__':main()
