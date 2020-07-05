## AWS SAM Application for Trips Management

This project goal is to provide simple APIs to create and list trips based on their country, city or date. It's built in Java and runs in the AWS stack, with SAM, API Gateway, Lambda and DynamoDB.

## Requirements

* AWS CLI already configured with at least PowerUser permission
* [Java SE Development Kit 8 installed](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Docker installed](https://www.docker.com/community-edition)
* [Maven](https://maven.apache.org/install.html)
* [SAM CLI](https://github.com/awslabs/aws-sam-cli)
* [Python 3](https://docs.python.org/3/)

## Setup process

### Installing dependencies

We use `maven` to install our dependencies and package our application into a JAR file:

```bash
mvn install
```

### Local development

**Invoking function locally through local API Gateway**
1. Start DynamoDB Local in a Docker container. 
  - On Linux and Mac  `docker run -p 8000:8000 -v $(pwd)/local/dynamodb:/data/ amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -dbPath /data`
  - On Windows `docker run -p 8000:8000 -v //C/local/dynamo:/data/ amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -dbPath /data`
  
  As an alternative, the above command can be replaced by using the docker-compose file in this repository root directory, through the following command
  `docker-compose up -d`
  
2. Create the DynamoDB table. 
`aws dynamodb create-table --table-name trip --attribute-definitions AttributeName=country,AttributeType=S AttributeName=travelDate,AttributeType=S --key-schema AttributeName=country,KeyType=HASH AttributeName=travelDate,KeyType=RANGE --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000`

The table can be deleted with the following command, in case it was already created before: `aws dynamodb delete-table --table-name trip --endpoint-url http://localhost:8000`

3. Start the SAM local API.
 - On a Mac: `sam local start-api --env-vars src/test/resources/test_environment_mac.json`
 - On Windows: `sam local start-api --env-vars src/test/resources/test_environment_windows.json`
 - On Linux: `sam local start-api --env-vars src/test/resources/test_environment_linux.json`
 
 OBS:  If you already have the container locally (in your case the java8), then you can use --skip-pull-image to remove the download

If the previous command ran successfully, you will be able to make a call and get a response from the endpoints listed in the Postman collection available in the `src/test/resources/` folder

**SAM CLI** is used to emulate both Lambda and API Gateway locally and uses our `template.yaml` to
understand how to bootstrap this environment (runtime, where the source code is, etc.) - The
following excerpt is what the CLI will read in order to initialize an API and its routes:


## Packaging and deployment

AWS Lambda Java runtime accepts either a zip file or a standalone JAR file - We use the latter in
this example. SAM will use `CodeUri` property to know where to look up for both application and
dependencies:

Firstly, we need a `S3 bucket` where we can upload our Lambda functions packaged as ZIP before we
deploy anything - If you don't have a S3 bucket to store code artifacts then this is a good time to
create one:

```bash
export BUCKET_NAME=my_cool_new_bucket
aws s3 mb s3://$BUCKET_NAME
```

Next, run the following command to package our Lambda function to S3:

```bash
sam package \
    --template-file template.yaml \
    --output-template-file packaged.yaml \
    --s3-bucket $BUCKET_NAME
```

Next, the following command will create a Cloudformation Stack and deploy your SAM resources.

```bash
sam deploy \
    --template-file packaged.yaml \
    --stack-name trips-project \
    --capabilities CAPABILITY_IAM
```

> **See [Serverless Application Model (SAM) HOWTO Guide](https://github.com/awslabs/serverless-application-model/blob/master/HOWTO.md) for more details in how to get started.**

After deployment is complete you can run the following command to retrieve the API Gateway Endpoint URL:

```bash
aws cloudformation describe-stacks \
    --stack-name sam-orderHandler \
    --query 'Stacks[].Outputs'
```

# Appendix

## AWS CLI commands

AWS CLI commands to package, deploy and describe outputs defined within the cloudformation stack:

```bash
sam package \
    --template-file template.yaml \
    --output-template-file packaged.yaml \
    --s3-bucket REPLACE_THIS_WITH_YOUR_S3_BUCKET_NAME

sam deploy \
    --template-file packaged.yaml \
    --stack-name sam-orderHandler \
    --capabilities CAPABILITY_IAM \
    --parameter-overrides MyParameterSample=MySampleValue

aws cloudformation describe-stacks \
    --stack-name sam-orderHandler --query 'Stacks[].Outputs'
```
# Credits

For information enrichment purposes, this readme is based on the file found on the following repository:
[study-datalake](https://github.com/iworks-education/study-datalake)