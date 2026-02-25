# Intervirt

Intervirt is a rootless educational software to study networks. It is built with [Kotlin Multiplatform](https://kotlinlang.org/multiplatform/). Keep in mind that this project is **highly experimental!** It is **not** made for **production-grade containerization**, but for educational purposes. 

### Installation

Currently, only artifacts generated with Actions are provided. See the *Build* job artifacts. Only Linux binaries in *.deb* and *.rpm* formats are provided currently. Just install them like any other program.
Currently, it is required to execute the program with the following environment variables:

```env
VIRTUAL_CONTAINER_IO=true
VIRTUAL_AGENT_MODE=true
```

This is required because the Agent isn't finished yet.

### Licensing

Intervirt is licensed under the *GNU General Public License v3.0 or any later version*. See the *About* section in Intervirt to see the licenses of its dependencies.