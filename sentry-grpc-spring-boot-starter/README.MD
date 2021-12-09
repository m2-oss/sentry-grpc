# Spring boot starter для sentry

В build.gradle добавить

`
implementation("ru.vtblife:sentry-spring-boot-starter")
`

и в environment добавить две переменные

`
SENTRY_DSN=https://<key_sentry>@sentry.m2.ru/<project_number>
SENTRY_ENVIRONMENT=<dev/test/prod>
`
