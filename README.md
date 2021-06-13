# dev-blog
Simple blog web app made using Spring Boot + Vue.js

![Demo](https://user-images.githubusercontent.com/70838429/121809679-677e6800-cc66-11eb-96de-d455b752cd45.gif)

# project features
A blog engine implemented using the Spring Boot framework with the following features:
Registration, authentication and user authorization. Generating and checking captcha codes.
Working with the user profile. Restore a forgotten password by clicking on the link to the e-mail. Organization of storage of static content (uploading photos of users).
Publishing, editing, moderation, and searching for posts by name and tags.
Likes and dislikes, comments on posts and comments.
Sort posts by likes, dislikes, number of views, and time.
Calendar of posts.
Blog statistics.
Global blog settings available to the administrator.

# getting started
To launch the project you need to build it by Intelij Idea. MySQL Server shall be installed or you have to use cloud databases services.
You need to set up properties in application.yml - example for local database:

spring.datasource:
         url: jdbc:mysql://localhost:3306/blog?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
         username: .....
         password: .....

Use instruction of cloud database service to set up these properties properly, if use that service.
The first run of project will create database and will fill in the test data.

# Deployed project
Link to deployed project: http://mets-java-skillbox.herokuapp.com
Note that the free accounts starts about 30 seconds since first query.
