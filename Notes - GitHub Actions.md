# Notes On Setting Up for GitHub Actions

## Current State

| File                                         | Current State                                                                           | Previous State                                                              |
|----------------------------------------------|-----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| common/services/FileUploader.kt              | var  relativeFileList = fileList. map  {  it: Path  ->  uploadDirPath.relativize(it)  } | var  relativeFileList = fileList. map  {  uploadDirPath.relativize( it )  } |
| .github/workflows/continuous_integration.yml | java-version: 17                                                                        |                                                                             |
| .github/workflows/continuous_integration.yml | gradle-version: 8.0.2                                                                   |                                                                             |
|                                              |                                                                                         |                                                                             |
