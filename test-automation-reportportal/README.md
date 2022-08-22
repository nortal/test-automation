# ReportPortal.io in Test Automation Framework

## Report Portal setup

1. Install [Docker](https://docs.docker.com/engine/installation/) ([Engine](https://docs.docker.com/engine/installation/), [Compose](https://docs.docker.com/compose/install/)).

2. Deploy ReportPortal with command:

```bash
$ docker-compose -f docker-compose.yml -p reportportal up -d
```

3. Open in your browser IP address of deployed environment at port `8080`:

```
http://IP_ADDRESS:8080
```

4. Use these details for access (login\pass):

- `default\1q2w3e`
- `superadmin\erebus`

5. Add dependency to this module in your project.

6. Set `test-automation.report.endpoint` property in `application-override.yml` to the IP address of previously deployed ReportPortal. Also adjust `test-automation.report.rp.api.key` and `test-automation.report.rp.project` properties based on ReportPortal user information (which can be found in User Profile window).

7. Execute your tests and observe generated reports in ReportPortal.
