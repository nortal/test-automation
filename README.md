Preconfigured sample pipelines can be found at
https://gitlab.com/tmobile/DCD/test/dcp-test-automation/-/pipeline_schedules

## Samples "pipelines" supported:

### (automation-tests-pipeline)

Sample Parameters:
- CI_ENVIRONMENT_SLUG `dev`
- CUCUMBER_TAGS_FILTER `not @Products`
- RELEASE_PIPELINE_ACTION `test`
- RUN_MODE `--delta-import`
- BRANCH (dropdown selection)


### (automation-tests-parameterized)

Sample Parameters:
- CI_ENVIRONMENT_SLUG `dev`
- RELEASE_PIPELINE_ACTION `test`
- BRANCH (dropdown selection)
- BUILD_NAME_SUFFIX `-parameterized-automation`

### (automation-tests-golden-data-import)

This pipeline runs a FULL Golden Data imports only. It does NOT execute any tests.
Pipeline runs on a schedule and manual runs are **discouraged**
It runs: 9PM ish PST

Sample Parameters:
- CI_ENVIRONMENT_SLUG `dev`
- RELEASE_PIPELINE_ACTION `test`
- BRANCH (dropdown selection)
- RUN_MODE `--full-import`
- SKIP_TESTS `true`

### (automation-tests-pipeline-products)

This pipeline runs PRODUCTS automation tests. It also includes a delta GD import
Pipeline runs on a schedule it can also be triggered manually
It runs:
0-1AM ish PST
10-12AM ish EST  

Sample Parameters:
- CI_ENVIRONMENT_SLUG `dev`
- RELEASE_PIPELINE_ACTION `test`
- CUCUMBER_TAGS_FILTER `@Products`
- BRANCH (dropdown selection)
- RUN_MODE `--delta-import`
- BUILD_NAME_SUFFIX `-delta-products`

## Explanations:

#### BRANCH

Select the source branch from the dropdown

#### CI_ENVIRONMENT_SLUG

Possible values:
`dev`, `dev2`, `qat`, `rel`, `sbx`

Determines which environment will be used and passed to the automation framework and thus which environment will tests run against.

#### RELEASE_PIPELINE_ACTION = test

Always required, if you want tests to actually be executed. (Not passing this parameter would result in a simple framework build/compilation)

#### RUN_MODE (optional)
Possible values:
`--delta-import` or `--e2e-import`

#### CUCUMBER_TAGS_FILTER (optional)
e.g. `not @Products`
Cucumber parameters which will be passed to -Dcucumber.filter.tags=

#### BUILD_NAME_SUFFIX (optional)
anything e.g. `-parameterized-automation`
This value will be appended to the build name, which will in turn determinte the S3 folder where the report is saved.
It's important to pass this value for parameterized runs in order not to mix up test results.

**WARNING not all permutions are valid. If you do not pass parameters in combinations like presented in one of the above samples, unexpected results may happen. **