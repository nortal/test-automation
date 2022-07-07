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

5. Adjust `rp.api.key` and `rp.project` properties in [reportportal.properties](./src/main/resources/reportportal.properties) file based on ReportPortal user information (can be found in User Profile window).

6. Execute tests in this module and observe generated reports in ReportPortal.
