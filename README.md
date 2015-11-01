# ScriptJava
A console/script environment for Java... Ever wanted to use POJOs in your batch/shell-scripts or write an platform independent script? - Try this. 

## Getting started

[Download the runnable JAR](https://github.com/masinger/ScriptJava/releases) and place it somewhere in your directory structrue.

On windows you can create a batch file with the following content:

    java -jar <PATH_TO_DOWNLOADED_JAR> %1 %*
  
and place it in the same directory as the JAR-file. If you now add this directory to your PATH environment variable you can execute ScriptJava from the command line, by typing the name of the batch file you just created. The batch file will redirect all given parameters to the java application.

## First steps
### 1. Simple Java-Statement
You can execute simple Java-Statements like

    System.out.println("Hello World")

Note that you don't have to add a semicolon add the end, if the entered command is a single instruction.

Local variables are command-scoped. For example: If you execute

    int i = 42

the variable ''i'' won't be available in the next command. Trying to access it will give you the following response:

    System.out.println(i);
    [source error] no such field: i

But it is available in the same command.

    for(int i=0; i<42; i++) System.out.println( i + " is not 42")
will run just fine.

### 2. Variable-Allocation
If you want to use/access a variable in multiple commands you can ''allocate'' them with the alloc(...)-method:

    alloc(varName:String, value:Object)
This will invoke the values ''.getClass()'' method to discover the type to be used for this variable. If you just want to declare it without assignig it's value you may use:

    allocNull(varName:String, type:Class<?>)
It will create a variable of the specified type and name. The value will be initilaized with ''null''.

Use the

    vars()
method to print a list of allocated variables. You can drop allocated variables with the 

    dealloc(varName:String)
method.

### 3. Helping Hands
There are some methods for your convenience.

1. Display available Methods

        methods(someObject:Object)
will print the accessible methods of the given object.

2. System.out.println(...) shortcut

        println(someObject:Object); println(someInt:int); ... ; println(someBool:boolean)
is a shortcut for ''System.out.println(...)''

3. System.exit(...) shortcut

        exit()
        exit(statusCode:int)
is a shortcut for ''System.exit(...)'' that also performs an additional cleanup. This should be the preferred way to exit ScriptJava.

4. Import Packages

        importPackage(packageName:String)
will import the given package.

## Advanced tools and usage
There are many other features you can use. Including:
+ [Maven support](https://github.com/masinger/ScriptJava/wiki/Maven-Support)
+ [Import of static methods](https://github.com/masinger/ScriptJava/wiki/Static-Imports)
+ Interactive script writing




