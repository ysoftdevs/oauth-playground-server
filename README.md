# ysoftdevs/oauth-playground-server

[![OAuth playground server deploy](https://github.com/ysoftdevs/oauth-playground-server/actions/workflows/build_and_deploy.yml/badge.svg?branch=master)](https://github.com/ysoftdevs/oauth-playground-server/actions/workflows/build_and_deploy.yml)

Welcome to OAuth Playground Server source repo.

Together with [ysoftdevs/oauth-playground-client](https://github.com/ysoftdevs/oauth-playground-client), these two
projects illustrate and explain basics of OAuth grants and Passkeys (WebAuthN).

See it in action: https://www.oauth-playground.online

> [!WARNING]
> Disclaimer: This is a demo. It is not to be used in production. It contains security vulnerabilities, some of them on
> purpose.
>
> If you are thinking about integrating OAuth into your project, have a look
> at [existing libraries](https://oauth.net/code/).
> If you insist you have to write it yourself, then familiarize yourself
> with [the OAuth 2.1 RFC](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09) and make sure you understand
> the risks.

## How to build and run locally

The project is written in Quarkus framework, so the easies way is to
use [Quarkus CLI](https://quarkus.io/guides/cli-tooling).

Then you can run:

```shell
quarkus dev
```

Alternatively, you may use the maven syntax:

```shell
./mvnw quarkus:dev
```

To compile a fat jar with dependencies, run:

```shell
/mvnw install
```