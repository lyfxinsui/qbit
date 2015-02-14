package io.advantageous.qbit;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.core.reflection.ClassMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.boon.Boon.puts;

/**
 * Main interface to QBit.
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class QBit {
    private Logger logger = LoggerFactory.getLogger(QBit.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();

    public  static Factory factory() {
           return new QBit().doGetFactory();
    }

   public  Factory doGetFactory() {
        Factory factory =  FactorySPI.getFactory();

        if (factory == null) {

            if (debug) {
                puts("Factory was null");
            }

            registerReflectionAndJsonParser();
            registerNetworkStack();
            return FactorySPI.getFactory();
        }

        return factory;
    }

    private void registerReflectionAndJsonParser() {
        try {
            final Class<?> boonFactory = Class.forName("io.advantageous.qbit.spi.RegisterBoonWithQBit");
            ClassMeta.classMeta(boonFactory).invokeStatic("registerBoonWithQBit");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not find reflection lib or JSON parser");
        }
    }

    private  void registerNetworkStack() {

        try {

            try {
                final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.vertx.RegisterVertxWithQBit");
                ClassMeta.classMeta(vertxFactory).invokeStatic("registerVertxWithQBit");

            } catch (Exception ex) {


                if (debug) {
                    puts("Unable to load vertx network stack, trying Jetty", ex);
                }

                final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.http.jetty.RegisterJettyWithQBit");
                ClassMeta.classMeta(vertxFactory).invokeStatic("registerJettyWithQBit");
            }
        }catch (Exception ex) {
            FactorySPI.setHttpServerFactory((host, port, manageQueues, pollTime, requestBatchSize,
                                             flushInterval, maxRequests, systemManager) -> {
                throw new IllegalStateException("Unable to load Vertx or Jetty network libs");
            });

            FactorySPI.setHttpClientFactory((host, port, requestBatchSize, timeOutInMilliseconds, poolSize,
                                             autoFlush, flushRate, keepAlive, pipeLine) -> {
                throw new IllegalStateException("Unable to load Vertx or Jetty network libs");
            });
        }
    }

}
