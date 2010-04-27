REM set JAVA_OPTS=-Xmx1000M -XX:MaxDirectMemorySize=500M
java %JAVA_OPTS% -cp deegree-tools-3.0-pre4.jar;. org.deegree.tools.ToolBox %*
