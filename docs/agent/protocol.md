# Intervirt Agent API Specification

This document contains the API specification of the *Intervirt Agent* which acts as a server for the Intervirt client.

### WebSocket Endpoints

The client requires one endpoint to be available via unencrypted WebSocket (`ws://`). It should be accessible through `ws://localhost:55436/containerManagement` via `GET`.

Messages from the client will always be encoded in JSON. Messages from the server will also always be encoded in JSON. The programm will also send commands to the server. Below, you will see example requests and corresponding example responses.

##### Add container

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.AddContainer",
    "id": "computer-93281", // Name of the container
    "ipv4": "192.168.0.145", // IPv4 address of the container
    "ipv6": "fd00:3452:2312:ab32:cdaa:4444:aaaa:bcda", // IPv6 address of the container
    "mac": "2a:5b:22:67:67:67", // MAC address of the container
    "internet": false, // Public internet access of the container
    "image": "debian/13", // Image of the container
    "type": "AddContainer" // Command of the request (this field is available in every request body).
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 already exists" // Error message
    "code": 3 // Status code of the error
}
```

##### Remove container

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.RemoveContainer",
    "id": "computer-93281", // Name of the container to remove
    "command": "removeContainer"
}
```

Successful message from server:

```json
{
    "progress": 0.9, // Percentage of progress (0.9 means 90%)
    "output": "Shutting down container..." // Progress message
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist." // Error message
    "code": 6
}
```

##### Set IPv4 address

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.IdWithNewIpv4",
    "id": "computer-93281", // Name of the container
    "newIP": "192.168.0.133", // New IPv4 address of the container
    "command": "setIPv4"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

##### Set IPv6 address

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.IdWithNewIpv6",
    "id": "computer-93281", // Name of the container
    "newIP": "fd00:3452:2312:ab32:cdaa:4444:bbbb:bcda", // New IPv6 address of the container
    "command": "setIPv6"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

##### Connect

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Connect",
    "id1": "computer-93281", // Name of the first container to connect
    "id2": "computer-31222", // Name of the second container to connect
    "command": "connect"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 and computer-31222 don't exist"
    "code": 6
}
```

You should connect both containers via networking.

##### Disconnect

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Disconnect",
    "id1": "computer-93281", // Name of the first container to disconnect
    "id2": "computer-31222", // Name of the second container to disconnect
    "command": "disconnect"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 and computer-31222 don't exist"
    "code": 6
}
```

##### Set internet access

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.SetInternetAccess",
    "id": "computer-93281",
    "enabled": true, // If internet access should be enabled
    "command": "setInternetAccess"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

##### Add port forwarding

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.AddPortForwarding",
    "id": "computer-93281",
    "internalPort": 80 // Port in the container to forward
    "externalPort": 8080, // External port to forward
    "command": "addPortForwarding"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

##### Remove port forwarding

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.RemovePortForwarding",
    "externalPort": 8080, // External port of the port forwarding to remove
    "command": "removePortForwarding"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

##### Wipe

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Command",
    "command": "wipe"
}
```

Successful message from server:

```json
{
    "progress": 0.9,
    "output": "Removing container computer-XXXXX"
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "code": 2 // Status code of error
}
```

##### Update

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Command",
    "command": "update"
}
```

Successful message from server:

```json
{
    "progress": 0.9,
    "output": "Updating ssh to X.X.X..."
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "code": 2 // Status code of error
}
```

This command should perform a system upgrade on the guest and also update all containers and restart them.

##### Shutdown

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Command",
    "command": "shutdown"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "code": 2
}
```

This should shutdown all containers and the guest.

##### Reboot

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Command",
    "command": "reboot"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "code": 2
}
```

This should shutdown all containers and reboot the guest.

##### Get version

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.Command",
    "command": "version"
}
```

Message from server:

```json
{
    "version": "1.2.3" // Version of the Intervirt Agent
			"canRunCommands": true // if the Agent can run commands on real containers
}
```

Acquires the version of the Intervirt Agent. This command must not fail.

##### Run command on container

If the agent responds in the version request with canRunCommands = false, this command may fail with error code 6.

Message from client:

```json
{
			"type": "io.github.bommbomm34.intervirt.data.RequestBody.RunCommand",
    "id": "computer-93281",
    "command": "whoami" // Command to run on the shell
}
```

Successful message from server:

```json
{
    "output": "root", // Output of the command
}
```

Failed message from server:

```json
{
    "error": "Not enough permissions to run the command" // Output of the command in a stream
    "code": 1 // Error code (in Intervirt manner, not Unix!)
}
```

### Intervirt Error Codes

Intervirt has its own error codes:

| Error code | Description                                                                                | Error text required |
|------------|--------------------------------------------------------------------------------------------|---------------------|
| 1          | There is an error available, but it's not defined by the Intervirt error codes explicitly. | yes                 |
| 2          | There was an unsuccessful operation, but no error was given.                               | no                  |
| 3          | The operation was already performed.                                                       | no                  |
| 4          | There was an error associated with the guest system.                                       | yes                 |
| 5          | There was an error in a command execution on the guest system                              | yes                 |
| 6          | The requested or needed resource was not found.                                            | yes                 |

### RESTful Endpoints

There is also two regular API endpoints via REST without WebSockets:

Endpoint: `GET http://localhost:55436/disk?id=ID_OF_THE_CONTAINER`

The Intervirt Agent should export the container as an archive and return it in this request.

Endpoint: `POST http://localhost:55436/disk?id=ID_OF_THE_CONTAINER`

The request body contains the container archive in binary form. The file is streamed by the client. It is sended as a multipart. Intervirt Agent should load the container filesystem contained in the archive in the container associated with the given id (name).

Endpoint: `POST http://localhost:55436/file?id=ID_OF_THE_CONTAINER&path=DESTINATION_PATH`

The request body contains a file in binary form. The file is streamed by the client. It is sended as a multipart. Intervirt Agent should construct a file of it and copy it to the container filesystem of the container with the given id and save it to the given path.

If there is an error, please report it via HTTP Status Codes. The container archive should always be a .tar.gz file.

Endpoint: `GET http://localhost:55436/file?id=ID_OF_THE_CONTAINER&path=DESTINATION_PATH`

Intervirt Agent should export the file of the container with the given id from the given path and return it in this request.

If there is an error, please report it via HTTP Status Codes. The container archive should always be a .tar.gz file.

### Tips

- Some commands in WebSockets require a stream instead a single answer (e.g. `runCommand`).
- Every client request in WebSockets will contain the field `type`.
- The server should listen on all interfaces on port 55436

If you have questions, simply contact me and I'll answer you as soon as possible.