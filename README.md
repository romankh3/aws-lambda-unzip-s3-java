## Overview
Files are extracted in place in the same bucket as where the zip file was uploaded. Any files present with the same name are overwritten. 
The zip file is deleted at the end of the operation.

## Necessary permissions
In order to remove the uploaded zip file, the role configured in your Lambda function should have a policy looking like this:
```
{
        "Effect": "Allow",
        "Action": [
            "s3:GetObject",
            "s3:PutObject",
            "s3:DeleteObject"
        ],
        "Resource": [
            "arn:aws:s3:::mybucket"
	]
}
```

## Handler Configuration
Handler property should be configured to `com.github.romankh3.awslambdas3.unzip.AwsLambdaS3UnzipFiles::handleRequest`

## Packaging for deployment
Maven is already configured to package the .jar file correctly for deployment into Lambda. Just run
```
mvn clean package
```
The packaged file will be present in your `target/` folder.

## PLan
Planning to add:
*   removing zip file after unzip it
*   process only .zip files
*   added test for unzip process(DONE)

## Release Notes
Can be found in [RELEASE_NOTES](RELEASE_NOTES.md).

## Authors
* Roman Beskrovnyi - [romankh3](https://github.com/romankh3)

## Acknowledgments
...

## Contributing
Please, follow [Contributing](CONTRIBUTING.md) page.

## Code of Conduct
Please, follow [Code of Conduct](CODE_OF_CONDUCT.md) page.

## License
This project is Apache License 2.0 - see the [LICENSE](LICENSE) file for details
