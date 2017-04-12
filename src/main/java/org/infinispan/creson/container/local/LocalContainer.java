package org.infinispan.creson.container.local;

import org.infinispan.AdvancedCache;
import org.infinispan.creson.container.BaseContainer;
import org.infinispan.creson.filter.FilterConverterFactory;
import org.infinispan.creson.object.Call;
import org.infinispan.creson.object.CallFuture;
import org.infinispan.creson.object.Reference;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Pierre Sutra
 */
public class LocalContainer extends BaseContainer {

   private static Map<BasicCache,Listener> listeners = new HashMap<>();
   private static Map<BasicCache,FilterConverterFactory> factories = new HashMap<>();

   private static synchronized UUID installListener(BasicCache cache){
      if (!listeners.containsKey(cache)) {
         FilterConverterFactory factory = new FilterConverterFactory();
         Listener listener = new Listener();
         ((AdvancedCache) cache).addListener(
               listener,
               factory.getFilterConverter(new Object[] { listener.getId() }),
               null);
         log.info("Local listener " + listener.getId() + " installed on " + cache);
         listeners.put(cache, listener);
         factories.put(cache, factory);
      }
      return listeners.get(cache).getId();
   }
   
   private UUID listenerID;
   private AdvancedCache<Reference,Call> cache;
      
   public LocalContainer(
         BasicCache c,
         Class clazz,
         Object key,
         boolean readOptimization,
         boolean forceNew,
         Object... initArgs)
         throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException,
         InterruptedException,
         ExecutionException, NoSuchMethodException, InvocationTargetException, TimeoutException, NoSuchFieldException {
      super(clazz, key, readOptimization, forceNew, initArgs);
      this.cache = ((org.infinispan.Cache)c).getAdvancedCache();
      listenerID = installListener(cache);
      if (log.isTraceEnabled()) log.trace(this+"Created successfully");
   }

   @Override 
   public UUID listenerID() {
      return listenerID;
   }

   @Override
   public void putAsync(Reference reference, Call call) {
      cache.putAsync(reference, call);
   }

   @Override
   public BasicCache getCache() {
      return cache;
   }

   @org.infinispan.notifications.Listener(sync = true, clustered = true)
   private static class Listener{
      
      private UUID id;
      
      public Listener(){
         id = UUID.randomUUID();
      }
      
      public UUID getId(){
         return id;
      }
      
      @Deprecated
      @CacheEntryModified
      @CacheEntryCreated
      public void onCacheModification(CacheEntryEvent event){
         CallFuture ret = (CallFuture) event.getValue();
         handleFuture(ret);
      }
      
      
   }
   
}