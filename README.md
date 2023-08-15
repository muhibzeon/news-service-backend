# news-service-backend
This is a simple Java/Maven/Spring Boot application that is used for a news service where any publisher can create a news article and publish it on the application page.

### How to Run
<hr>
This App uses Spring boot version 2.7.3 and JDK 1.8+.<br>

- Clone this Repository.
- Make sure you are using appropriate JDK version(above 1.8). However, the application should run fine if the Java version is not below 8.
- Preferably run the application in IntelliJ IDE to avoid any sort of version issues.

At this point there will be a database error. In order to solve it, switch to `src/main/resources/application.properties` file, change to your own you database url, username and password if applicable.
- Create a schema called `magazine` in the database.

Now spring boot will automatically create all the necessary tables after you manually create a schema called `magazine`. 

- Re-run the application and it will run without any error(s) anymore.

**MySQL Database Modification**
<br><hr>
Now switch to the `roles` table and populate the first three rows with these three values:
- `ROLE_ADMIN`
- `ROLE_AUTHOR`
- `ROLE_USER`

The `id` column populates itself with Auto increment. The application will be able to create new users based on their roles and perform necessary actions.

**Create Admin**<br><hr>
To create an Admin please utilize other API services like `Postman`. However, it is also possible to perform this task from IntelliJ IDE. Switch to `src/main/java/com.magazine.backend/controllers/AuthController.java` class and move to line 75. There will be an earth icon, click it and select `Generate request in HTTP Client`, it will also do the similar task.
```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "username": "admin2",
  "name": "Admin Smith",
  "role": [
    "admin"
  ],
  "password": "012345"
}
```
`Admin` user can only be created using this procedure as it is restricted for any user to become an admin while registering. Please `Signup` with few users based on their roles(user/author) and test the application.


