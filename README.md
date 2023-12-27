# later.jdbc

A small library on top of [`honeysql`](https://github.com/seancorfield/honeysql/tree/develop),
[`manifold`](https://github.com/clj-commons/manifold),
[`next.jdbc`](https://github.com/seancorfield/next-jdbc),
[`virtual`](https://github.com/bnert-land/virtual).

This library "glues" the above together and executes the underlying
queries via a virtual thread per query.

## Motivation

I didn't want to worry about queries blocking, and wanted to be able to express query values as manifold deferreds.
I didn't see much out there (not that I looked very hard), and decided to give it a go myself.

There are two "flavors" of the api, sync and async (via manifold). Async operations are postfixed w/ a `&` (inspired from XTDB).

Take a look at [`dev/src/user.clj`](./dev/src/user.clj) for some high level examples.

This library is very young, use at you own discretion.
