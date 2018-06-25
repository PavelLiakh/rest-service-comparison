# rest-service-comparison
Tool to make rest services comparison by access speed and smart comparison of responses.

Executable version can be found in folder 'executable'.

User gradle task 'run' to run application.
Add -PappArgs="--file=<filename, ex.: post_example.json> (optional)--logLevel=debug" to pass parameters.
Add -Pdebug to start in debug mode. 

Use gradle task 'fatJar' to build jar including all dependencies.

Run jar with '--file=<filename>' to pass file with parameters.
Run jar with '--debug' to set debug log level.

