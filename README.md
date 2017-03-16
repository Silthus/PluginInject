# PluginInject

PluginInject is a brand new dependency injection framework for Bukkit plugins built around HK2, the reference implementation of JSR 330(Dependency Injection for Java). PluginInject primarily provides the scaffolding needed to easily create a plugin utilizing HK2's
service discovery features rather than implementing them itself.

## Overview

**Features:**
* Generate bindings at compile time between Contracts(interfaces) and Services(implementations).
* Automatic registration of Listeners.
* Module system for processing initialization.
* Built-in bindings for common server components and plugin dependencies.

**What PluginInject is NOT:**
* Dependency Injection implementation(HK2 is included for this).
* Annotation-based plugin.yml generator.

### Why HK2? Why not Guice?

HK2 is the most feature complete of any dependency injection system that I've come across.  It provides full lifecycle support(@PostConstruct, @PreDestroy), service discovery at compile time(no need to scan the classpath), and is designed around
plugin-type bindings that allow multiple implementations of a given contract.

Alternatives that were considered:
* Guice: No lifecycle support, but even with Netflix Governator it lacked compile-time bindings.  Governator's auto-binding additionally requires the use of a separate annotation to mark something as being bound automatically. Plugin-type bindings(Guice Multibindings) are provided as an extension and aren't as well supported.
* Dagger: No lifecycle support, I'm personally not a fan of their @Provides system.  Does have very good support for compile-time bindings.  Not sure how easily it can be integrated with plugins.  Does support multibindings, although it's a little ugly.
* PicoContainer: No support for multibindings or compile-time bindings.

### Does PluginInject support injection between plugins?

This project intentionally avoids dealing with injections between plugins.  Interactions between
plugins that may be disabled or enabled at will falls much more within the realm of OSGI and
introduces complexity that is detrimental toward providing dependency injection for an isolated
project.  PluginInject does however provide named injections of all plugins on the server(described below),
and any plugin that extends ```InjectedPlugin``` will expose its ServiceLocator in a consistent
manner.

### How can I add new injection features to my plugin?
API docs for HK2 can be found [here](https://hk2.java.net/2.5.0-b36/apidocs/index.html).  Not all functionality
in the API docs is included with PluginInject, as PluginInject only includes hk2-locator. The [main site](https://hk2.java.net/2.5.0-b36/index.html) for HK2
includes tutorials and detailed examples on a wide array of functionality. HK2 functionality generally can be
modified by implementing specific interfaces, meaning a plugin is capable of providing almost any new injection
behavior. Because plugin-provided binders are applied before the configuration is loaded, it's even possible
to completely replace the loading system or change where the descriptors are read from.

## Using PluginInject
Any plugin using PluginInject should list it as a dependency in its plugin.yml such that
PluginInject is loaded before it.

The general lifecycle of a plugin using PluginInject on load is as follows:

1. Server ServiceLocator is instantiated.
1. Plugin specific ServiceLocator is instantiated.
1. Plugin provided bindings and built-in bindings are applied to the ServiceLocator
1. PluginInject pre-load features are applied
1. Plugin specific ServiceLocator loads services using the DynamicConfigurationService.
1. PluginInject post-load features are applied.
1. All Modules are eagerly instantiated.
1. InjectedPlugin instance is injected into.
1. PostConstruct is invoked on the InjectedPlugin instance.

The general lifecycle of a plugin using PluginInject on shutdown is as follows:

1. PreDestroy is called on InjectedPlugin instance.
1. Plugin specific ServiceLocator is shutdown, destroying all loaded services.

### Writing Your JavaPlugin Class
Plugins wishing to utilize PluginInject should extend ```com.natemortensen.plugininject.InjectedPlugin```,
which will automatically handle loading the inhabitants files and the creation of the ServiceLocator.  Plugins
that extend InjectedPlugin will be unable to use ```onEnable``` or ```onDisable``` for initialization and cleanup,
and should instead use methods marked with ```javax.annotation.PostConstruct``` or ```javax.annotation.PreDestroy```.

If a plugin is unable to extend PluginInject, they can also take advantage of injection using ```com.natemortensen.plugininject.Injection```.
  Check out the source of InjectedPlugin for an example of how to do that.
### Structuring Your Plugin
PluginInject is structured around Modules, which are conceptually small pieces of your plugin's logic.  In practice,
however, they can be pretty much anything.  They are simply the demand applied to the dependency injection system
that triggers action.  They are eagerly instantiated after the plugin has been enabled.

Any Service that is initialized and an instance of ```org.bukkit.Listener``` is automatically registered. This behavior
can be disabled for an individual service by annotating it with ```com.natemortensen.plugininject.listener.DoNotRegister```.
To change this behavior on a larger scale, a plugin can provide a binding for ```com.natemortensen.plugininject.listener.ListenerFilter```
and filter out additional Listeners.

### Generating Bindings at Compile Time
Bindings are generated at compile time by depending on PluginInject, as it includes HK2's Metadata generator,
an annotation processor which scans for ```org.jvnet.hk2.annotations``` and creates descriptors of all services.
Anything that is an interface meant to be provided by injection should be annotated with ```@Contract```. Anything
that implements a contract and is meant to be automatically provided should be annotated with ```@Service```. As such,
all modules should be annotated with ```@Service``` and implement ```Module```.

Maven dependency that will handle everything:
```xml
        <dependency>
            <groupId>com.natemortensen.plugininject</groupId>
            <artifactId>core</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
```

PluginInject is currently not hosted anywhere, and will have to be built locally.  If you want to host it let me know and I'll
link it here.

### Providing Bindings at Run time
PluginInject does not support any sort of automatic scanning at run time. Instead, plugins can override ```InjectedPlugin#getBinders```
and provide Binders which explicitly define the bindings.  For convenience it's recommended to extend ```org.glassfish.hk2.utilities.binding.AbstractBinder```
when implementing a Binder. Note that AbstractBinder's methods are in the form ```bind(x).to(y)``` where x is an instance of y, whereas
Guice's methods are in the form ```bind(y).to(x)```. This is so that an individual service can be bound to multiple contracts.
HK2 does not bind a service to its own type either, that must be done explicitly.  In HK2 the default scope for a class-based binding is
PerLookup, and is most likely not what you want.

### Built-in Bindings
Server-wide:
* Server
* PluginManager
* ScoreboardManager
* ItemFactory
* Messenger
* BukkitScheduler
* Every plugin, bound to its own type, JavaPlugin, and Plugin, named the plugin name

Plugin-specific:
* The plugin instance, bound to its own type, JavaPlugin, and Plugin.  Because it is provided
in the plugin-specific locator, it will injected in place of the other plugin bindings unless ```@Named``` is used
to qualify the desired plugin.
* Logger

## Examples
An example of a complete plugin can be found [here](https://github.com/evilmidget38/PluginInject/tree/master/example/src/main/java/example).  In order for a plugin to utilize PluginInject, all it needs is the added dependency.  Annotation processors included within PluginInject will generate bindings
during compilation.

For the sake of brevity I've excluded all bukkit import statements.

### InjectedPlugin Structure
```java
import com.natemortensen.plugininject.InjectedPlugin;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

// All we have to do is extend InjectedPlugin and injection happens automatically.
public class Plugin extends InjectedPlugin
{
	// Some service that we defined somewhere else in our plugin
	@Inject
	SomeService service;
	
	// Handle initialization in a method annotated with @PostConstruct, or implement
	// the interface org.glassfish.hk2.api.PostConstruct.
	@PostConstruct
	public void init()
	{
		getLogger().info("Enabled!");
	}
	
	// Handle shutdown in a method annotated with @PreDestroy, or implement
	// the interface org.glassfish.hk2.api.PreDestroy.
	@PreDestroy
	public void end()
	{
		getLogger().info("Disabled!");
	}
	
	// InjectedPlugin implements Listener and is automatically registered.
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		event.getPlayer().kickPlayer("no.");
	}
}
```

### Writing a Listener

```java
import com.natemortensen.plugininject.Module;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

// Mark it as a Service so that it's automatically bound
@Service
// Implement Module so that we're eagerly initialized.  If we didn't implement Module
// then our listener would only be registered when someone wanted to inject ExampleListener.
// If no one ever injected ExampleListener then ExampleListener wouldn't be created, and wouldn't be registered.
// Implement Listener because we're a listener
public class ExampleListener implements Module, Listener
{
	
	// This is our main plugin instance.
	@Inject
	JavaPlugin plugin;
	
	// Write handlers as you normally would
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		event.getPlayer().kickPlayer("no.");
	}
}

```

### Defining a Service

```java
import com.natemortensen.plugininject.Module;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// Mark our contract
@Contract
interface SomeContract
{
	String foo();
}
// Mark our implementation
@Service
// Higher rank than OtherService means that SomeService will be injected
// when the dependency is on SomeContract
@Rank(1)
class SomeService implements SomeContract
{
	@Override
	public String foo()
	{
		return "abc";
	}
}
// Mark another implementation
@Service
class OtherService implements SomeContract, Listener
{
	// Explicitly inject SomeService
	@Inject
	private SomeService someService;
	@Override
	public String foo()
	{
		return "def";
	}

	// Any service can be a Listener, even if it's not a module.
	// It is only registered if it is created though, so this
	// only works because either a module or the InjectedPlugin instance
	// depend on SomeContract
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		event.getEntity().sendMessage(someService.foo());
	}
}
// Define a module that depends on the
@Service
class OurModule implements Module, org.glassfish.hk2.api.PostConstruct
{
	// Inject all instances of SomeContract.
	// This is ordered, according to the rank of the services.
	// Therefore SomeService comes before OtherService.
	@Inject
	IterableProvider<SomeContract> services;
	// Our plugin's logger
	@Inject
	Logger logger;

	@Override
	public void postConstruct()
	{
		//Output is abcdef
		logger.info(StreamSupport.stream(services.spliterator(), false).map(SomeContract::foo).collect(Collectors.joining()));
	}
}
```
## Future Plans
PluginInject will be developed as I have time and as I find it useful.

* 0.1 Release - Configuration System


