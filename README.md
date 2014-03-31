# Carousel

"Then the carousel started, and I watched her go round and round..."

Carousel is a library for creating and registering functions that
might be useful in the lifecycle of your application. It includes
macros for generating these functions, and some utilities for calling
them.

Although you can define you own lifecycle verbs via defregistrar,
we've included some baked-in verbs, namely: definit, defstart,
defstop, and defstatus. There's also a mechanism for invoking
administrative commands using a server-socket called defadmin.

## License

Copyright © 2014 Sonian, Inc.

Distributed under the Apache 2.0 License.
