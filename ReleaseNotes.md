<img align="right" width="200" height="37" src="media/Gematik_Logo_Flag.png"/> <br/>

# Release notes

## Release 2.0.0 (2025-03-21)

- Updated:
  - Spring Parent to 2.7.2
  - Renamed flags for activating relaxed validation and outcome validation log level

## Release 1.6.9 (2025-03-21)

- Removed:
  - JWT Validation

## Release 1.6.7 (2025-02-21)

- Update:
  - Dependency-Updates (CVEs et al.)
  - Extended logging
  - Add context-information (provenance) before pdf will be generated

## Release 1.5.3 (2024-04-04)

- first official GitHub-Release

## Internal Releases - non official GitHub-Releases 

| Version | Acceptance | Productive | Notes | Tickets |
|---------|------------|------------|-------|---------|
| 1.3.5   | x.x.2023   | 28.06.2023 | repeatable bundle id generation. notification id in receipt bundle. spring boot update. logs contain client id | |
| 1.3.4   | 24.04.2023 | not productiv | cve fixes | |
| 1.3.3   | 31.03.2023 | not productiv | k8s release | |
| 1.3.2   | 07.03.2023 | 15.03.2023 | logging switch to json format | |
| 1.3.1   | 09.02.2023 | 22.02.2023 | ik extraction fallback | |
| 1.3.0   | 31.01.2023 | 01.02.2023 | spring boot upgrade | |
| 1.2.0   | 10.01.2023 | 11.01.2022 | threadsafe parsing and extended logging | |
| 1.1.0   | 06.12.2022 | 06.12.2022 | remove address check with hls. fix some critical or blocking Vulnerabilities and Exposures | |
| 1.0.2   | 11.11.2022 | 16.11.2022 | | |
| 1.0.1   | 24.10.2022 | 26.10.2022 | | |
| 1.0.0   | 12.09.2022 | 14.09.2022 | wrong configuration for pdfgen-service client. sets wrong bundle id in receipt bundle. bug with content type in response to user. | DSC2-2946, DSC2-2885, DSC2-2867 |
