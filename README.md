# Getting Started

### Reference Documentation

Service uses Liquibase for DB versioning framework and H2 database.
It is configured as in-mem DB and it reinitializes on every start-up.
To switch H2 to persisted mode change datasource in
[application.properties](src%2Fmain%2Fresources%2Fapplication.properties)
`spring.datasource.url=jdbc:h2:~/liquibase;`

Please provide access key for public service `api.exchangerate.host/`
[application.properties](src%2Fmain%2Fresources%2Fapplication.properties)
`exchange.rate.service.accessKey=`

Example API requests are here:
[api-requests.http](.req%2Fapi-requests.http)