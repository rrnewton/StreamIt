package at.dms.kjc.linprog;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISolver extends Remote {
    // solves a problem
    double[] solveOverRMI(CPLEXSolve model) throws RemoteException;
    // returns name of open port to connect to
    String getOpenPort() throws RemoteException;
    // clears a binding of name <name>
    void clearPort(String name) throws RemoteException;
}
