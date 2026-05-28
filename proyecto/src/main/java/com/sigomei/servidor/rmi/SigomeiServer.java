package com.sigomei.servidor.rmi;

import com.sigomei.servidor.config.AppConfig;
import com.sigomei.servidor.config.ServerLog;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;

public class SigomeiServer {

    public static void main(String[] args) throws Exception {
        int registryPort = AppConfig.getInt("rmi.registry.port", 1099);
        Registry registry = LocateRegistry.createRegistry(registryPort);
        SigomeiRemote service = new SigomeiRemoteImpl();
        registry.rebind("SIGOMEI", service);
        ServerLog.info("Servidor SIGOMEI iniciado en puerto RMI " + registryPort);
        System.out.println("Servidor SIGOMEI iniciado. Servicio RMI: SIGOMEI puerto " + registryPort);
        System.out.println("Presione Ctrl + C para detener el servidor.");
        new CountDownLatch(1).await();
    }
}
