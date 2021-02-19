## About jLichat
This is a Java library implementing the client protocol for the [Lichat](https://shirakumo.github.io/lichat-protocol) protocol.

## How To
The `Client` class gives you a functioning TCP client using the standard Sockets API. In order to process events as they arrive, extend the `HandlerAdapter` or do your own dispatching with the `Handler` interface. You can add as many handlers as you want to the client through the `addHandler` class.

The rest should be pretty self-explanatory by the source and Lichat documentation.
