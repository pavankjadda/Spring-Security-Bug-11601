# Spring-Security-Bug-11601

## How to Run?
1. Run the application in IDE after changing [DB properties](src/main/resources/application.yml)
2. Insert table data [from resources](src/main/resources/scripts/db.sql) in DB.
3. Replace the [AD configuration](src/main/java/com/example/springsecuritybug11601/security/SecurityConfig.java#L70=) with your config
4. Run the following curl command, and it will fail with 401 error while it's supposed to execute successfully by taking Active Directory authentication.
    ```shell
   curl --location --request GET 'http://localhost:8080/api/v1/search' \
   --header 'Authorization: Basic YWRtaW46YWRtaW4='
    ```
