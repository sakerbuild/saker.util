# saker.util

[![Build status](https://img.shields.io/azure-devops/build/sakerbuild/6bb2b4fa-d3da-44b0-85ad-b3e21ea3bab6/10/master)](https://dev.azure.com/sakerbuild/saker.util/_build) [![Latest version](https://mirror.nest.saker.build/badges/saker.util/version.svg)](https://nest.saker.build/package/saker.util "saker.util | saker.nest")

General Java utilities library. The library contains various functions and classes that may be useful when developing applications.

The library is also included in the [saker.build system](https://saker.build) under the `saker.build.thirdparty` package.

See the [documentation](https://saker.build/saker.util/doc/) for more information.

## Build instructions

The project uses the [saker.build system](https://saker.build) for building. It requires JDK8 and JDK9 to build. Use the following command to build the project:

```
java -jar path/to/saker.build.jar -bd build -EUsaker.java.jre.install.locations=path/to/jdk8;path/to/jdk9 compile saker.build
```

## License

The source code for the project is licensed under *GNU General Public License v3.0 only*.

Short identifier: [`GPL-3.0-only`](https://spdx.org/licenses/GPL-3.0-only.html).

Official releases of the project (and parts of it) may be licensed under different terms. See the particular releases for more information.
