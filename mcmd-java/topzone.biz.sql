create database IF NOT EXISTS topzone;
use topzone;

CREATE TABLE domains (domain TEXT, module TEXT);
INSERT INTO domains VALUES('topzone.biz','user_defined');
CREATE TABLE companies (company TEXT, description TEXT, module TEXT);
INSERT INTO companies VALUES('Topzone','none','user_defined');
CREATE TABLE netblocks (netblock TEXT, module TEXT);
CREATE TABLE locations (latitude TEXT, longitude TEXT, street_address TEXT, module TEXT);
CREATE TABLE vulnerabilities (host TEXT, reference TEXT, example TEXT, publish_date TEXT, category TEXT, status TEXT, module TEXT);
CREATE TABLE ports (ip_address TEXT, host TEXT, port TEXT, protocol TEXT, module TEXT);
CREATE TABLE hosts (host TEXT, ip_address TEXT, region TEXT, country TEXT, latitude TEXT, longitude TEXT, module TEXT);
INSERT INTO hosts VALUES('topzone.biz','54.153.125.159',NULL,NULL,NULL,NULL,'hackertarget');
CREATE TABLE contacts (first_name TEXT, middle_name TEXT, last_name TEXT, email TEXT, title TEXT, region TEXT, country TEXT, module TEXT);
CREATE TABLE credentials (username TEXT, password TEXT, hash TEXT, type TEXT, leak TEXT, module TEXT);
CREATE TABLE leaks (leak_id TEXT, description TEXT, source_refs TEXT, leak_type TEXT, title TEXT, import_date TEXT, leak_date TEXT, attackers TEXT, num_entries TEXT, score TEXT, num_domains_affected TEXT, attack_method TEXT, target_industries TEXT, password_hash TEXT, password_type TEXT, targets TEXT, media_refs TEXT, module TEXT);
CREATE TABLE pushpins (source TEXT, screen_name TEXT, profile_name TEXT, profile_url TEXT, media_url TEXT, thumb_url TEXT, message TEXT, latitude TEXT, longitude TEXT, time TEXT, module TEXT);
CREATE TABLE profiles (username TEXT, resource TEXT, url TEXT, category TEXT, notes TEXT, module TEXT);
CREATE TABLE repositories (name TEXT, owner TEXT, description TEXT, resource TEXT, category TEXT, url TEXT, module TEXT);
CREATE TABLE dashboard (module TEXT, runs INT);
INSERT INTO dashboard VALUES('recon/domains-contacts/pgp_search',2);
INSERT INTO dashboard VALUES('recon/domains-contacts/whois_pocs',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/bing_domain_api',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/bing_domain_web',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/builtwith',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/certificate_transparency',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/google_site_api',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/hackertarget',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/netcraft',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/shodan_hostname',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/ssl_san',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/threatcrowd',2);
INSERT INTO dashboard VALUES('recon/domains-hosts/vpnhunter',2);
INSERT INTO dashboard VALUES('recon/companies-contacts/bing_linkedin_cache',4);
INSERT INTO dashboard VALUES('recon/companies-multi/github_miner',2);
INSERT INTO dashboard VALUES('recon/companies-multi/whois_miner',2);
INSERT INTO dashboard VALUES('recon/contacts-credentials/hibp_breach',2);
INSERT INTO dashboard VALUES('recon/contacts-credentials/hibp_paste',2);
INSERT INTO dashboard VALUES('recon/contacts-profiles/fullcontact',2);
INSERT INTO dashboard VALUES('recon/profiles-profiles/twitter_mentioned',2);
INSERT INTO dashboard VALUES('recon/profiles-profiles/twitter_mentions',2);
INSERT INTO dashboard VALUES('recon/profiles-repositories/github_repos',2);
INSERT INTO dashboard VALUES('recon/netblocks-companies/whois_orgs',2);
INSERT INTO dashboard VALUES('recon/netblocks-hosts/shodan_net',2);
INSERT INTO dashboard VALUES('recon/netblocks-ports/census_2012',2);
INSERT INTO dashboard VALUES('recon/netblocks-ports/censysio',2);
INSERT INTO dashboard VALUES('recon/ports-hosts/migrate_ports',2);
INSERT INTO dashboard VALUES('recon/hosts-hosts/bing_ip',2);
INSERT INTO dashboard VALUES('recon/hosts-hosts/ssltools',2);
INSERT INTO dashboard VALUES('recon/hosts-ports/shodan_ip',2);
