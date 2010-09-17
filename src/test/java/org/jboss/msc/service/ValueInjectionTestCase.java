package org.jboss.msc.service;

import org.jboss.msc.scott.HostManager;
import org.jboss.msc.scott.LoggingServiceListener;
import org.jboss.msc.services.LifecycleService;
import org.jboss.msc.value.InjectedValue;
import org.jboss.msc.value.Value;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author scott.stark@jboss.org
 * @verion $Id$
 */
public class ValueInjectionTestCase
{
   static class ValueSource {
      private String id;
      ValueSource() {
         this("defaultID");
      }
      ValueSource(String id) {
         this.id = id;
      }
      public String toString() {
         return super.toString() + ":" + id;
      }
   }
   static class ValueSinkService implements Service<ValueSource> {
      private final InjectedValue<ValueSource> sourceInjector = new InjectedValue<ValueSource>();
      private final InjectedValue<List<ValueSource>> sourcesInjector = new InjectedValue<List<ValueSource>>();

      @Override
      public void start(StartContext context) throws StartException {
         System.out.println("ValueSinkService.start");
      }

      @Override
      public void stop(StopContext context) {
         System.out.println("ValueSinkService.stop");
      }

      @Override
      public ValueSource getValue() throws IllegalStateException {
         System.out.println("ValueSinkService.getValue");
         System.out.flush();
         return sourceInjector.getValue();
      }
      public List<ValueSource> getValues() throws IllegalStateException {
         System.out.println("ValueSinkService.getValues");
         System.out.flush();
         return sourcesInjector.getValue();
      }

      public InjectedValue<ValueSource> getSourceInjector() {
         System.out.println("ValueSinkService.getSourceInjector");
         System.out.flush();
         return sourceInjector;
      }

      public InjectedValue<List<ValueSource>> getSourcesInjector() {
         System.out.println("ValueSinkService.getSourcesInjector");
         System.out.flush();
         return sourcesInjector;
      }
   }
   static class ValueSourceService implements Service<ValueSource> {
       private ValueSource target;

      ValueSourceService() {
      }
      @Override
      public void start(StartContext context) throws StartException {
         System.out.println("ValueSourceService.start, creating ValueSource");
         System.out.flush();
         target = new ValueSource();
      }

      @Override
      public void stop(StopContext context) {
         System.out.println("ValueSourceService.stop");
         System.out.flush();
      }

      @Override
      public ValueSource getValue() throws IllegalStateException {
         System.out.println("ValueSourceService.getValue");
         System.out.flush();
         return target;
      }
   }
      static class ValueSourceListService implements Service<List<ValueSource>> {
       private List<ValueSource> target;

      ValueSourceListService() {
      }
      @Override
      public void start(StartContext context) throws StartException {
         System.out.println("ValueSourceListService.start, creating ValueSources list");
         System.out.flush();
         target = new ArrayList<ValueSource>();
         target.add(new ValueSource("ID0"));
         target.add(new ValueSource("ID1"));
         target.add(new ValueSource("ID2"));
      }

      @Override
      public void stop(StopContext context) {
         System.out.println("ValueSourceListService.stop");
         System.out.flush();
      }

      @Override
      public List<ValueSource> getValue() throws IllegalStateException {
         System.out.println("ValueSourceListService.getValue");
         System.out.flush();
         return target;
      }
   }

   @Test
   public void testSetup() throws ServiceRegistryException
   {
      final ServiceContainer serviceContainer = ServiceContainer.Factory.create();
      final BatchBuilder builder = serviceContainer.batchBuilder();


      ValueSinkService vssink = new ValueSinkService();
      ValueSourceService vssource = new ValueSourceService();
      ValueSourceListService vssourceList = new ValueSourceListService();

      ServiceName vssinkName = ServiceName.of("ValueSinkService");
      ServiceName vssourceName = ServiceName.of("ValueSourceService");
      ServiceName vssourceListName = ServiceName.of("ValueSourceListService");

      // Add the source services
      builder.addService(vssourceName, vssource);
      builder.addService(vssourceListName, vssourceList);
      // Add the sink service with dependencies on the source
      final BatchServiceBuilder<ValueSource> vssourceBuilder = builder.addService(vssinkName, vssource);

      vssourceBuilder.addDependency(vssourceName, ValueSource.class, vssink.getSourceInjector());
      List<ValueSource> tmp = new ArrayList<ValueSource>();
      vssourceBuilder.addDependency(vssourceListName, (Class<List<ValueSource>>) tmp.getClass(), vssink.getSourcesInjector());

      builder.install();
      // Test the wiring
      System.out.println("Checking for injection of source into sink...");
      System.out.println(vssink.getValue());
      System.out.flush();
      System.out.println("Checking for injection of source list into sink...");
      System.out.println(vssink.getValues());
      System.out.flush();
      System.out.println("Shutting down...");
      serviceContainer.shutdown();
   }


}
