# Deployment

- Using the dist task
The dist task builds a binary version of your application that you can deploy to a server without any dependency on sbt, the only thing the server needs is a Java installation.

found in `/target/universal/app-name.app-version.zip`

This produces a ZIP file containing all JAR files needed to run your application in the `target/universal` folder of your application.

To run the application, unzip the file on the target server, and then run the script in the `bin` directory. The name of the script is your application name, and it comes in two versions, a bash shell script, and a windows .bat script.

```sh
$ unzip app-name.app-version.zip
$ app-name.app-version/bin/app-name -Dplay.http.secret.key=ad31779d4ee49d5ad5162bf1429c32e2e9933f3b

```

```sh
Universal / packageXzTarball
#playreact-1.2.4.txz
Universal / packageZipTarball
#playreact-1.2.4.tgz
```
- The Native Packager
`Play uses the sbt Native Packager plugin`. The native packager plugin declares the `dist` task to create a zip file. Invoking the `dist` task is directly equivalent to invoking the following:

`Universal / packageBin`


## Play PID Configuration
`Play manages its own PID`, which is described in the Production configuration.

Since Play uses a separate pidfile, we have to provide it with a proper path, which is `packageName.value` here. The name of the pid file must be `play.pid`. In order to tell the startup script where to place the PID file, put a file `application.ini` inside the `dist/conf` folder and add the following content:

`s"-Dpidfile.path=/var/run/${packageName.value}/play.pid"`
# Add all other startup settings here, too
Please see the sbt-native-packager page on Play for more details.

To prevent Play from creating a PID just set the property to `/dev/null`:

`-Dpidfile.path=/dev/null` // you are using systemd


What is the Docker Gateway?
When you create a Docker bridge network, Docker automatically sets up a virtual subnet. Within that subnet:

The containers get assigned IP addresses.

The host gets an IP address too — this is called the gateway.


Containers use the gateway to communicate with the host or to reach external networks (via NAT).

`The gateway is the IP address of the bridge interface on the host within the network’s subnet.`

```conf
 play.crypto.secret="{{repl ConfigOption "app_secret"}}"
 play.http.forwarded.trustedProxies = ["{{repl ConfigOption "docker_gateway"}}"]
 play.i18n.langs = [ "en" ]
 pidfile.path = "/dev/null"
 log.override.path = "/opt/data/logs"
```

```conf
# Trust reverse proxies. This is essential on AWS.
play.http.forwarded.trustedProxies=["0.0.0.0/0", "::/0"]
# If you have a modern proxy with "Forwarded:" headers, set this to "rfc7239":
play.http.forwarded.version=x-forwarded
play.http.forwarded.version=${?OV_HTTP_FORWARD_LOGIC}
```


Separation of concerns with two kinds of plugins

- format plugins define how a package is created
- archetype plugins define what a package should contain
`Mappings` define how your build files should be organized on the target system.

`Mappings` are a `Seq[(File, String)]`, which translates to “a list of tuples, where each tuple defines a source file that gets mapped to a path on the target system”.

# Deploying to Heroku

## Deploying to a remote Git repository
- Store your application in git
```git
$ git init
$ git add .
$ git commit -m "init"
```
- Create a new application on Heroku

```sh
$ heroku create
Creating warm-frost-1289... done, stack is cedar-14
http://warm-frost-1289.herokuapp.com/ | git@heroku.com:warm-frost-1289.git
Git remote heroku added
```

This provisions a new application with an HTTP (and HTTPS) endpoint and Git endpoint for your application. The Git endpoint is set as a new remote named `heroku` in your Git repository’s configuration.

- Deploy your application
To deploy your application on Heroku, use Git to push it into the `heroku` remote repository:

```sh
$ git push heroku main
Counting objects: 93, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (84/84), done.
Writing objects: 100% (93/93), 1017.92 KiB | 0 bytes/s, done.
Total 93 (delta 38), reused 0 (delta 0)
remote: Compressing source files... done.
remote: Building source:
remote:
remote: -----> Play 2.x - Scala app detected
remote: -----> Installing OpenJDK 1.8... done
remote: -----> Priming Ivy cache (Scala-2.11, Play-2.4)... done
remote: -----> Running: sbt compile stage
...
remote: -----> Dropping ivy cache from the slug
remote: -----> Dropping sbt boot dir from the slug
remote: -----> Dropping compilation artifacts from the slug
remote: -----> Discovering process types
remote:        Procfile declares types -> web
remote:
remote: -----> Compressing... done, 93.3MB
remote: -----> Launching... done, v6
remote:        https://warm-frost-1289.herokuapp.com/ deployed to Heroku
remote:
remote: Verifying deploy... done.
To https://git.heroku.com/warm-frost-1289.git
* [new branch]      main -> main

```

git push command is `git push <remote> <branch>`

`git push heroku main`

- Check that your application has been deployed

```sh
$ heroku ps
=== web (Free): `target/universal/stage/bin/sample-app -Dhttp.port=${PORT}`
web.1: up 2015/01/09 11:27:51 (~ 4m ago)
```

The web process is up and running. We can view the logs to get more information:

`heroku logs`


## Set Allowed Hosts:
By default, Play ships with a list of default Allowed Hosts filter. This is the list of allowed valid hosts = ["localhost", ".local", "127.0.0.1"]. You need to add an option to allow Railway hosts, [".up.railway.app"].
Add the following to the application.conf file:
play.filters.hosts.allowed=[".up.railway.app"]

`play.filters.hosts.allowed`
Protects against HTTP Host header attacks.

```conf
play.http {
  secret.key = "fHRqIol/>tn26elksjdfilasgddsfhgyuj4356zzSDf"
 #session.domain = ".playscala.cn"
  session.maxAge = 31536000000
  parser.maxMemoryBuffer = 10MB
  parser.maxDiskBuffer = 20MB
  forwarded.trustedProxies=["::1", "127.0.0.1"] # Protects against spoofed IP addresses or HTTPS status in reverse proxy setups.
}

```


### Built-in Filters
- Configuring gzip encoding
- Configuring security headers
 -Configuring CORS
- Configuring CSP
- Configuring allowed hosts
- Configuring Redirect HTTPS filter
- Configuring the IP filter

### Default filters
- play.filters.csrf.CSRFFilter
- play.filters.headers.SecurityHeadersFilter
- play.filters.hosts.AllowedHostsFilter

`play.http.forwarded.trustedProxies = ["10.0.0.0/8", "127.0.0.1"]`

Only requests coming from IPs in 10.0.0.0/8 or 127.0.0.1 will have their X-Forwarded-* headers trusted.

Requests from any other IP will have those headers ignored.

when a request passes through a proxy, certain request information is sent using either the standard `Forwarded` header or `X-Forwarded-*` headers. For example, instead of reading the `REMOTE_ADDR` header (which will now be the IP address of your reverse proxy), the user's true IP will be stored in a standard `Forwarded: for="..."` header or a `X-Forwarded-For` header.

If you don't configure play to look for these headers, you'll get incorrect information about the client's IP address, whether or not the client is connecting via HTTPS, the client's port and the hostname being requested.

`You need to tell Play to trust proxy headers and extract real client info from them.`

```conf
play.http.forwarded.trustXForwarded = true
play.http.forwarded.trustedProxies = ["0.0.0.0/0"] 
```

