@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  market-prices-lk startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and MARKET_PRICES_LK_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\market-prices-lk-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\ktor-server-config-yaml-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-server-netty-jvm-3.5.0.jar;%APP_HOME%\lib\koog-agents-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-event-handler-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-memory-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-snapshot-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-tokenizer-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-trace-jvm-1.0.0.jar;%APP_HOME%\lib\agents-features-opentelemetry-jvm-1.0.0.jar;%APP_HOME%\lib\agents-core-jvm-1.0.0.jar;%APP_HOME%\lib\ktor-server-sse-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-server-cio-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-server-core-jvm-3.5.0.jar;%APP_HOME%\lib\yamlkt-jvm-0.13.0.jar;%APP_HOME%\lib\kaml-jvm-0.79.0.jar;%APP_HOME%\lib\http-client-ktor-jvm-1.0.0.jar;%APP_HOME%\lib\ktor-client-apache5-jvm-3.5.0.jar;%APP_HOME%\lib\embeddings-llm-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-bedrock-client-jvm-1.0.0.jar;%APP_HOME%\lib\ktor-client-content-negotiation-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-client-logging-jvm-3.5.0.jar;%APP_HOME%\lib\prompt-processor-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-cached-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-model-jvm-1.0.0.jar;%APP_HOME%\lib\ktor-client-cio-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-client-core-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-http-cio-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-serialization-kotlinx-json-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-serialization-kotlinx-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-websocket-serialization-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-serialization-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-websockets-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-network-tls-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-http-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-events-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-network-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-sse-jvm-3.5.0.jar;%APP_HOME%\lib\ktor-utils-jvm-3.5.0.jar;%APP_HOME%\lib\prompt-executor-anthropic-client-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-ollama-client-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-openai-client-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-openai-client-base-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-executor-clients-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-structure-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-cache-files-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-cache-model-jvm-1.0.0.jar;%APP_HOME%\lib\agents-tools-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-markdown-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-tokenizer-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-xml-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-model-jvm-1.0.0.jar;%APP_HOME%\lib\prompt-llm-jvm-1.0.0.jar;%APP_HOME%\lib\kotlinx-serialization-json-io-jvm-1.11.0.jar;%APP_HOME%\lib\kotlinx-serialization-core-jvm-1.11.0.jar;%APP_HOME%\lib\utils-jvm-1.0.0.jar;%APP_HOME%\lib\agents-mcp-metadata-jvm-1.0.0.jar;%APP_HOME%\lib\agents-utils-jvm-1.0.0.jar;%APP_HOME%\lib\embeddings-base-jvm-1.0.0.jar;%APP_HOME%\lib\http-client-core-jvm-1.0.0.jar;%APP_HOME%\lib\rag-base-jvm-1.0.0.jar;%APP_HOME%\lib\serialization-jackson-1.0.0.jar;%APP_HOME%\lib\serialization-core-jvm-1.0.0.jar;%APP_HOME%\lib\kotlinx-schema-generator-json-jvm-0.4.4.jar;%APP_HOME%\lib\kotlinx-schema-generator-core-jvm-0.4.4.jar;%APP_HOME%\lib\kotlinx-schema-json-jvm-0.4.4.jar;%APP_HOME%\lib\kotlinx-serialization-json-jvm-1.11.0.jar;%APP_HOME%\lib\kotlinx-coroutines-jdk9-1.11.0.jar;%APP_HOME%\lib\kotlinx-coroutines-reactive-1.11.0.jar;%APP_HOME%\lib\kotlinx-coroutines-slf4j-1.11.0.jar;%APP_HOME%\lib\ktor-io-jvm-3.5.0.jar;%APP_HOME%\lib\implementation-jvm-0.3.0.jar;%APP_HOME%\lib\compat-jvm-0.3.0.jar;%APP_HOME%\lib\core-jvm-0.3.0.jar;%APP_HOME%\lib\exporters-core-jvm-0.3.0.jar;%APP_HOME%\lib\bedrockruntime-jvm-1.6.72.jar;%APP_HOME%\lib\aws-config-jvm-1.6.72.jar;%APP_HOME%\lib\aws-http-jvm-1.6.72.jar;%APP_HOME%\lib\aws-endpoint-jvm-1.6.72.jar;%APP_HOME%\lib\aws-json-protocols-jvm-1.6.12.jar;%APP_HOME%\lib\aws-xml-protocols-jvm-1.6.12.jar;%APP_HOME%\lib\aws-protocol-core-jvm-1.6.12.jar;%APP_HOME%\lib\aws-event-stream-jvm-1.6.12.jar;%APP_HOME%\lib\aws-signing-default-jvm-1.6.12.jar;%APP_HOME%\lib\http-auth-aws-jvm-1.6.12.jar;%APP_HOME%\lib\aws-signing-common-jvm-1.6.12.jar;%APP_HOME%\lib\http-client-engine-default-jvm-1.6.12.jar;%APP_HOME%\lib\http-client-engine-okhttp-jvm-1.6.12.jar;%APP_HOME%\lib\http-client-jvm-1.6.12.jar;%APP_HOME%\lib\aws-core-jvm-1.6.72.jar;%APP_HOME%\lib\smithy-client-jvm-1.6.12.jar;%APP_HOME%\lib\aws-credentials-jvm-1.6.12.jar;%APP_HOME%\lib\http-auth-jvm-1.6.12.jar;%APP_HOME%\lib\http-auth-api-jvm-1.6.12.jar;%APP_HOME%\lib\http-jvm-1.6.12.jar;%APP_HOME%\lib\identity-api-jvm-1.6.12.jar;%APP_HOME%\lib\telemetry-defaults-jvm-1.6.12.jar;%APP_HOME%\lib\logging-slf4j2-jvm-1.6.12.jar;%APP_HOME%\lib\telemetry-api-jvm-1.6.12.jar;%APP_HOME%\lib\serde-json-jvm-1.6.12.jar;%APP_HOME%\lib\serde-xml-jvm-1.6.12.jar;%APP_HOME%\lib\serde-form-url-jvm-1.6.12.jar;%APP_HOME%\lib\serde-jvm-1.6.12.jar;%APP_HOME%\lib\runtime-core-jvm-1.6.12.jar;%APP_HOME%\lib\sdk-common-jvm-0.3.0.jar;%APP_HOME%\lib\okhttp-coroutines-5.3.2.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.11.0.jar;%APP_HOME%\lib\jackson-databind-2.21.3.jar;%APP_HOME%\lib\jackson-core-2.21.3.jar;%APP_HOME%\lib\jackson-module-kotlin-2.21.3.jar;%APP_HOME%\lib\kotlin-reflect-2.3.21.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-2.3.10.jar;%APP_HOME%\lib\snakeyaml-engine-kmp-jvm-3.1.1.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-2.3.10.jar;%APP_HOME%\lib\kotlin-logging-jvm-8.0.01.jar;%APP_HOME%\lib\kotlinx-io-core-jvm-0.9.0.jar;%APP_HOME%\lib\noop-jvm-0.3.0.jar;%APP_HOME%\lib\model-jvm-0.3.0.jar;%APP_HOME%\lib\api-ext-jvm-0.3.0.jar;%APP_HOME%\lib\platform-implementations-jvm-0.3.0.jar;%APP_HOME%\lib\sdk-api-jvm-0.3.0.jar;%APP_HOME%\lib\semconv-jvm-0.3.0.jar;%APP_HOME%\lib\urlencoder-lib-jvm-1.6.0.jar;%APP_HOME%\lib\kotlinx-io-bytestring-jvm-0.9.0.jar;%APP_HOME%\lib\kotlinx-schema-annotations-jvm-0.4.4.jar;%APP_HOME%\lib\api-jvm-0.3.0.jar;%APP_HOME%\lib\java-typealiases-jvm-0.3.0.jar;%APP_HOME%\lib\okhttp-jvm-5.3.2.jar;%APP_HOME%\lib\okio-jvm-3.17.0.jar;%APP_HOME%\lib\kotlinx-datetime-jvm-0.7.1.jar;%APP_HOME%\lib\kotlin-stdlib-2.4.0.jar;%APP_HOME%\lib\logback-classic-1.5.35.jar;%APP_HOME%\lib\tabula-1.0.5.jar;%APP_HOME%\lib\pdfbox-3.0.3.jar;%APP_HOME%\lib\annotations-26.0.2-1.jar;%APP_HOME%\lib\logback-core-1.5.35.jar;%APP_HOME%\lib\slf4j-simple-1.7.32.jar;%APP_HOME%\lib\httpclient5-5.5.1.jar;%APP_HOME%\lib\slf4j-api-2.0.18.jar;%APP_HOME%\lib\fontbox-3.0.3.jar;%APP_HOME%\lib\pdfbox-io-3.0.3.jar;%APP_HOME%\lib\commons-logging-1.3.3.jar;%APP_HOME%\lib\jts-core-1.18.1.jar;%APP_HOME%\lib\bcmail-jdk15on-1.69.jar;%APP_HOME%\lib\bcpkix-jdk15on-1.69.jar;%APP_HOME%\lib\bcutil-jdk15on-1.69.jar;%APP_HOME%\lib\bcprov-jdk15on-1.69.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\commons-csv-1.9.0.jar;%APP_HOME%\lib\gson-2.8.7.jar;%APP_HOME%\lib\jai-imageio-jpeg2000-1.4.0.jar;%APP_HOME%\lib\jai-imageio-core-1.4.0.jar;%APP_HOME%\lib\jbig2-imageio-3.0.3.jar;%APP_HOME%\lib\config-1.4.8.jar;%APP_HOME%\lib\netty-codec-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.2.13.Final.jar;%APP_HOME%\lib\alpn-api-1.1.3.v20160715.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.2.13.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-http-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-compression-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-protobuf-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-marshalling-4.2.13.Final.jar;%APP_HOME%\lib\netty-handler-4.2.13.Final.jar;%APP_HOME%\lib\netty-codec-base-4.2.13.Final.jar;%APP_HOME%\lib\netty-transport-classes-kqueue-4.2.13.Final.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.2.13.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.2.13.Final.jar;%APP_HOME%\lib\netty-transport-4.2.13.Final.jar;%APP_HOME%\lib\netty-buffer-4.2.13.Final.jar;%APP_HOME%\lib\netty-resolver-4.2.13.Final.jar;%APP_HOME%\lib\netty-common-4.2.13.Final.jar;%APP_HOME%\lib\opentelemetry-sdk-1.61.0.jar;%APP_HOME%\lib\opentelemetry-sdk-trace-1.61.0.jar;%APP_HOME%\lib\opentelemetry-sdk-metrics-1.61.0.jar;%APP_HOME%\lib\opentelemetry-sdk-logs-1.61.0.jar;%APP_HOME%\lib\opentelemetry-sdk-common-1.61.0.jar;%APP_HOME%\lib\opentelemetry-api-1.61.0.jar;%APP_HOME%\lib\opentelemetry-context-1.61.0.jar;%APP_HOME%\lib\opentelemetry-common-1.61.0.jar;%APP_HOME%\lib\httpcore5-h2-5.3.6.jar;%APP_HOME%\lib\httpcore5-5.3.6.jar;%APP_HOME%\lib\jackson-annotations-2.21.jar;%APP_HOME%\lib\reactive-streams-1.0.3.jar


@rem Execute market-prices-lk
@rem endlocal doesn't take effect until after the line is parsed and variables are expanded
@rem which allows us to clear the local environment before executing the java command
endlocal & "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MARKET_PRICES_LK_OPTS%  -classpath "%CLASSPATH%" io.ktor.server.netty.EngineMain %* & call :exitWithErrorLevel

:exitWithErrorLevel
@rem Use "%COMSPEC%" /c exit to allow operators to work properly in scripts
"%COMSPEC%" /c exit %ERRORLEVEL%
