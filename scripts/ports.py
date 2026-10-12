"""Version-specific API adapters. Shared UI/decoder code stays in the two source trees."""
import json
from pathlib import Path
import shutil

LEGACY=('1.20','1.20.1','1.20.2','1.20.3','1.20.4','1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4')
API={
 '1.20':'0.83.0+1.20','1.20.1':'0.92.6+1.20.1','1.20.2':'0.91.6+1.20.2','1.20.3':'0.91.1+1.20.3','1.20.4':'0.97.3+1.20.4','1.20.5':'0.97.8+1.20.5','1.20.6':'0.100.8+1.20.6',
 '1.21':'0.102.0+1.21','1.21.1':'0.116.17+1.21.1','1.21.2':'0.106.1+1.21.2','1.21.3':'0.114.1+1.21.3','1.21.4':'0.119.4+1.21.4','1.21.5':'0.128.2+1.21.5','1.21.6':'0.128.2+1.21.6','1.21.7':'0.129.0+1.21.7','1.21.8':'0.136.1+1.21.8','1.21.9':'0.134.1+1.21.9','1.21.10':'0.138.4+1.21.10','1.21.11':'0.141.6+1.21.11',
 '26.1':'0.145.1+26.1','26.1.1':'0.145.4+26.1.1','26.1.2':'0.155.3+26.1.2','26.2':'0.161.0+26.2','26.3':'0.162.0+26.3'}

def sources(root,version):
    if version in ('1.20.1','1.21.11'):
        return root/('src120' if version=='1.20.1' else 'src'),root/('src120/videomenu.mixins.json' if version=='1.20.1' else 'videomenu.mixins.json')
    base='src120' if version in LEGACY else 'src'
    folder=root/'build'/'sources'/version
    if folder.exists(): shutil.rmtree(folder)
    shutil.copytree(root/base,folder)
    mixins=json.loads((root/('src120/videomenu.mixins.json' if base=='src120' else 'videomenu.mixins.json')).read_text())
    def edit(name,old,new):
        file=folder/'com/videomenu'/name
        content=file.read_text()
        if old not in content: raise RuntimeError('Missing API adapter anchor: '+name)
        file.write_text(content.replace(old,new))
    if version in LEGACY and version not in ('1.20','1.20.1'):
        edit('EvatexTitleScreen.java','method_25420(class_332 ctx)','method_25420(class_332 ctx,int mx,int my,float delta)')
        edit('VideoSelectScreen.java','method_25420(class_332 context)','method_25420(class_332 context, int mouseX, int mouseY, float delta)')
        edit('mixin/ServerBackgroundMixin.java','evatex$serverVideo(class_332 context, CallbackInfo ci','evatex$serverVideo(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci')
    if version in ('1.20.2','1.20.3','1.20.4'):
        edit('mixin/ServerListBackgroundMixin.java','    @Shadow private boolean field_26847;\n','')
        edit('mixin/ServerListBackgroundMixin.java','evatex$background=field_26846; evatex$edges=field_26847;','evatex$background=field_26846;')
        edit('mixin/ServerListBackgroundMixin.java','field_26846=false; field_26847=false;','field_26846=false;')
        edit('mixin/ServerListBackgroundMixin.java','field_26846=evatex$background; field_26847=evatex$edges;','field_26846=evatex$background;')
    if version in ('1.20.3','1.20.4'):
        edit('mixin/ServerListBackgroundMixin.java','method="method_25394"','method="method_48579"')
    if version in LEGACY and version.startswith('1.21'):
        edit('AstralTypography.java','new net.minecraft.class_2960("evatexbackgroundvideo","ui")','net.minecraft.class_2960.method_60655("evatexbackgroundvideo","ui")')
        edit('video/VideoBackground.java','new class_2960("evatexbackgroundvideo", "video")','class_2960.method_60655("evatexbackgroundvideo", "video")')
    if version in ('1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4'):
        file=folder/'com/videomenu/mixin/ServerBackgroundMixin.java'
        content=file.read_text();start=content.index('    @Inject(method = "method_25434"');file.write_text(content[:start]+'}\n')
        mixins['client'].remove('WorldSelectionBackgroundMixin')
        (folder/'com/videomenu/mixin/WorldSelectionBackgroundMixin.java').unlink()
        mixins['client'].append('MenuListBackgroundMixin')
        shutil.copyfile(root/'src/com/videomenu/mixin/MenuListBackgroundMixin.java', folder/'com/videomenu/mixin/MenuListBackgroundMixin.java')
        mixins['client'].remove('ServerListBackgroundMixin')
        (folder/'com/videomenu/mixin/ServerListBackgroundMixin.java').unlink()
        file=folder/'com/videomenu/mixin/TitleScreenMixin.java'
        content=file.read_text();start=content.index('    @Inject(method = "method_25394"');file.write_text(content[:start]+'}\n')
        file=folder/'com/videomenu/mixin/RotatingCubeMapRendererMixin.java'
        file.write_text((root/'src/com/videomenu/mixin/RotatingCubeMapRendererMixin.java').read_text().replace('IIZ)V','IIFF)V').replace('boolean rotate, CallbackInfo ci','float delta, float rotation, CallbackInfo ci'))
    if version in ('1.21.5','1.21.6','1.21.7','1.21.8'):
        edit('mixin/TitleScreenMixin.java','footer.method_25306(null)','footer.method_25306()')
    if version=='1.21.5':
        edit('AstralButton.java','matrix.pushMatrix();','matrix.method_22903();')
        edit('AstralButton.java','matrix.translate(x+w/2f,y+h/2f);','matrix.method_22904(x+w/2f,y+h/2f,0);')
        edit('AstralButton.java','matrix.rotate(clockwise ? (float)Math.PI/2 : -(float)Math.PI/2);','matrix.method_22907(new org.joml.Quaternionf().rotateZ(clockwise ? (float)Math.PI/2 : -(float)Math.PI/2));')
        edit('AstralButton.java','matrix.popMatrix();','matrix.method_22909();')
        edit('AstralDraw.java','matrix.pushMatrix()','matrix.method_22903()')
        edit('AstralDraw.java','matrix.translate((float)x,(float)y)','matrix.method_22904(x,y,0)')
        edit('AstralDraw.java','matrix.scale(scale,scale)','matrix.method_22905(scale,scale,scale)')
        edit('AstralDraw.java','matrix.popMatrix()','matrix.method_22909()')
    if version=='1.21.5':
        edit('mixin/RotatingCubeMapRendererMixin.java','IIZ)V','IIFF)V')
        edit('mixin/RotatingCubeMapRendererMixin.java','boolean rotate, CallbackInfo ci','float delta, float rotation, CallbackInfo ci')
    if version in ('1.21.2' ,'1.21.3','1.21.4'):
        edit('video/VideoBackground.java', 'context.method_25293(TEXTURE_ID, (width-dw)/2, (height-dh)/2, dw, dh,\n                0f, 0f, frame.width, frame.height, frame.width, frame.height);', 'context.method_25302(net.minecraft.class_1921::method_62277, TEXTURE_ID, (width-dw)/2, (height-dh)/2,\n                0f, 0f, dw, dh, frame.width, frame.height, frame.width, frame.height);')
    if version in ('1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10'):
        edit('AstralButton.java','method_75752','method_48579')
        edit('AstralHomeButton.java','method_75752','method_48579')
        edit('video/VideoTexture.java','field_63613 = RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR);','method_4527(true, false);')
        if version == '1.21.5':
            edit('video/VideoBackground.java','class_10799.field_56883, TEXTURE_ID','net.minecraft.class_1921::method_62277, TEXTURE_ID')
    if version == '1.20':
        edit('AstralButton.java','protected void method_48579','public void method_48579')
        edit('AstralHomeButton.java','protected void method_48579','public void method_48579')
    if version in ('1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10'):
        (folder/'com/videomenu/mixin/SplashManagerMixin.java').write_text((root/'src120/com/videomenu/mixin/SplashManagerMixin.java').read_text())
    if version.startswith('26.'):
        from named_port import symbols, translate
        mapping=symbols(root/'build/dependencies')
        mixins['client']=[x for x in mixins['client'] if not x.startswith('atlas.') and x!='RotatingCubeMapRendererMixin']
        mixins.pop('plugin',None)
        mixins['compatibilityLevel']='JAVA_25'
        (folder/'com/videomenu/AtlasCompatibility.java').unlink()
        shutil.rmtree(folder/'com/videomenu/mixin/atlas')
        (folder/'com/videomenu/mixin/RotatingCubeMapRendererMixin.java').unlink()
        edit('MenuShortcut.java','return AtlasCompatibility.isCustomUi();','return false;')
        file=folder/'com/videomenu/video/VideoBackground.java'
        content=file.read_text(); start=content.index('    public record AtlasFrame'); end=content.index('    public boolean render(',start); file.write_text(content[:start]+content[end:])
        for file in folder.rglob('*.java'):
            content=translate(file.read_text(),mapping)
            if version.startswith('26.'):
                content=content.replace('drawCenteredString','centeredText').replace('drawString','text')
                content=content.replace('net.minecraft.util.Util.getPlatform().openFile(VideoFiles.directory().toFile());','java.awt.Desktop.getDesktop().open(VideoFiles.directory().toFile());')
                content=content.replace('EditGameRulesScreen','WorldCreationGameRulesScreen')
                content=content.replace('GuiGraphics','GuiGraphicsExtractor').replace('renderBackground','extractBackground').replace('renderContents','extractContents').replace('renderListBackground','extractListBackground').replace('renderListSeparators','extractListSeparators')
                if file.name=='EvatexTitleScreen.java':content=content.replace('drawable.render(', 'drawable.extractRenderState(')
                if file.name=='AstralVolumeSlider.java':content=content.replace('renderWidget','extractWidgetRenderState')
                if file.name in ('VideoSelectScreen.java','TitleScreenMixin.java','EvatexTitleScreen.java'):
                    content=content.replace('public void render(', 'public void extractRenderState(').replace('super.render(', 'super.extractRenderState(').replace('method = "render"','method = "extractRenderState"').replace(';render(', ';extractRenderState(')
                if version in ('26.2','26.3'):
                    content=content.replace('method = "destroy"','method = "close"')
                    content=content.replace('.setScreen(','.gui.setScreen(').replace('.screen;', '.gui.screen();')
                if version=='26.3':
                    content=content.replace('com.mojang.blaze3d.textures.FilterMode','com.mojang.renderpearl.api.textures.FilterMode')
            file.write_text(content)
        file=folder/'com/videomenu/mixin/TitleScreenMixin.java'
        content=file.read_text(); i=content.rfind('}')
        background='extractBackground'
        gui='GuiGraphicsExtractor'
        content=content[:i]+f'''    @Inject(method="{background}", at=@At("HEAD"), cancellable=true)
    private void evatex$titleVideo(net.minecraft.client.gui.{gui} context, int mx, int my, float delta, CallbackInfo ci) {{
        if (VideoBackground.INSTANCE.render(context,width,height)) ci.cancel();
    }}
'''+content[i:]
        file.write_text(content)
    path=folder/'videomenu.mixins.json';path.write_text(json.dumps(mixins,indent=2)+'\n')
    return folder,path
