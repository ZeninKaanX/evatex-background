import com.videomenu.RecoveryCircuit;
public final class RecoveryCircuitRegression {
 public static void main(String[] args) {
  var circuit=new RecoveryCircuit();
  var cause=new IllegalStateException("broken EVATEX UI fixture");
  if(!circuit.trip(cause)||!circuit.suspended()||circuit.failure()!=cause)throw new AssertionError("Failed to suspend");
  if(circuit.trip(new RuntimeException("repeat"))||circuit.failure()!=cause)throw new AssertionError("Repeated recovery replaced original failure");
  var fatal=new RecoveryCircuit();
  if(fatal.trip(new OutOfMemoryError())||fatal.suspended())throw new AssertionError("VM-fatal error must not be swallowed");
  if(fatal.trip(new ThreadDeath()))throw new AssertionError("Thread termination must not be swallowed");
  System.out.println("PASS recovery is one-shot and excludes fatal JVM errors");
 }
}
