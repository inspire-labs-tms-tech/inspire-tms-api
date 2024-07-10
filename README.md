# inspire-tms-api

## Setup

### Environment Variables

The following variables must be defined in the runtime environment:

| Name              | Description                                                                       |
|-------------------|-----------------------------------------------------------------------------------|
| SUPABASE_URL      | The publicly accessible HTTP(S) URL (with port) where the Supabase API is running |
| SUPABASE_ANON_KEY | The anonymous (public) Supabase JWT key                                           |
| DB_USER           | Postgres user to connect with (admin-level)                                       |
| DB_PASS           | Password for the Postgres user                                                    |
| DB_HOST           | Host where Postgres is running                                                    |
| DB_PORT           | Port on the host where Postgres is running                                        |
| DB_NAME           | The name of the database in Postgres to connect to                                |
| SITE_URL          | The HTTP(S) URL where Inspire TMS is publicly accessible                          |
