DO $$
BEGIN
  IF current_database() <> 'cvtcaptchadb' THEN
    RAISE EXCEPTION 'This script can only be run on cvtcaptchadb!';
  END IF;
END
$$;


DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
ALTER SCHEMA public OWNER TO cvtcaptcha_app;
GRANT ALL ON SCHEMA public TO cvtcaptcha_app;
GRANT CREATE ON SCHEMA public TO cvtcaptcha_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cvtcaptcha_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cvtcaptcha_app;