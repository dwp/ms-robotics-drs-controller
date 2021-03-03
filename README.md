# ms-robotics-drs-controller

The `ms-robotics-drs-controller` microservice to transform data recieved from the frontend application and transform it to our upstream robotics service.

### Continuous Integration (CI) Pipeline

For general information about the CI pipeline on this repository please see documentation at: https://confluence.service.dwpcloud.uk/x/_65dCg


## Build & Run

Standard maven build.
* to package the `jar` file: `mvn clean package`
* to run the application execute: `java -jar /path/to/jar/app.jar server /path/to/config.yml`


###Local Cucumber Tests

To execute the Cucumber Tests locally the `docker-compose.yml` file can be utilised. This has been written to allow usage by both the pipeline as well as local invocation.

To run the Cucumber Tests locally please run the following command:
```
docker-compose up --exit-code-from cucumber-tests
```

Due to the orchestration within `docker-compose.yml` there is no need to stop any running services after the tests have completed as this functionality is built in.


### Pipeline Fragments

The CI uses the concept of 'pipeline fragments' to build up a pipeline of modular components. These can be configured by 'including' the relevant fragments in the `gitlab-ci/includes.yml`. Pipeline fragments are stored here https://gitlab.nonprod.dwpcloud.uk/wa-transformation/pipeline-fragments and each comes with a `README` detailing the specific instructions on use.  

***The CI pipeline contains a stage to check that up-to-date versions of each fragment are being used - the pipeline will FAIL if the fragments are too outdated.***

***The CI pipeline contains a stage to check that all the required fragments are included - this is to ensure a minimum level of quality is being met - the pipeline will FAIL if the required fragments are not present.***

The `includes.yml` file is referenced in the main `.gitlab-ci.yml` to pull in the relevant CI jobs

```yaml
include:
  - local: "/gitlab-ci/includes.yml"  
```

The relevant stages must also be defined in the main `.gitlab-ci.yml` e.g.

```yaml
stages:
  - update-version
  - code-quality
  - code-test
  - application-build
  - code-analysis
  - site-report
  - image-build
  - container-image-test
  - image-push
  - update-project-metadata
  - generate-api-docs
  - pages
  - open-source
  - create-schedules
```


### Variables

Some pipeline fragments require specific variables to be set at a project level - these are documented in the relevant `README`.

The following variables have been set at a `health` group level and are inherited into this repo so ***do not*** need to be set again:

`DEV_ACCOUNT_ID`  
`PROD_ACCOUNT_ID`  
`NEXUS_USER`  
`NEXUS_PASSWORD`  
`AWS_DEFAULT_REGION`  
`WRITE_REPOSITORY_TOKEN`  
`GITLAB_API_TOKEN`  

The following variables have been set at a `health/ns-esa/components` group level and are inherited into this repo so ***do not*** need to be set again:

`AWS_ACCESS_KEY_ID`   
`AWS_SECRET_ACCESS_KEY` 


### Additional stages/jobs

Additional stages/jobs can be added to the main `.gitlab-ci.yml` as necessary.


**Pipeline Invocation**

This CI Pipeline now replaces the Jenkins Build CI Process for the `ms-robotics-drs-controller`. 

Gitlab CI will automatically invoke a pipeline run when pushing to a feature branch (this can be prevented using `[skip ci]` in your commit message if not required).

When a feature branch is merged into `develop` it will automatically start a `develop` pipeline and build the required artifacts.

For production releases please see the release process documented at: https://confluence.service.dwpcloud.uk/pages/viewpage.action?spaceKey=DHWA&title=SRE
A production release requires a manual pipeline (to be invoked by an SRE) this is only a release function. 
Production credentials are required.


### Schedules

The CI pipeline has a stage which sets up a schedule to run the `develop` branch every night - the schedule can be found in the `CI/CD/Schedules` section of Gitlab.


## Production Release

To create production artefacts the following process must be followed https://confluence.service.dwpcloud.uk/display/DHWA/SRE


**Access**

Therefore please continue to use `branch-develop` or `branch-f-*` (depending on branch name) for proving any feature changes.

While this repository is open internally for read, no one has write access to this repository by default.
To obtain access to this repository please contact #ask-health-platform within slack and a member will grant the appropriate level of access.
