def sep = File.separator
def pool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
def rnd = new java.security.SecureRandom()

// 64 ASCII characters are exactly the 512 bit the cookie secrets require
def secret = { (1..64).collect { pool.charAt(rnd.nextInt(pool.length())) }.join() }

// The project is generated into request.outputDirectory, which differs from the working directory e.g. with -DoutputDirectory or in IDE wizards
def config = new File(request.outputDirectory + sep + request.artifactId + sep +
        "src" + sep + "main" + sep + "resources" + sep + "config.yaml")

// Every placeholder gets its own value, so dev and test share no secret
config.write(config.getText("UTF-8").replaceAll(/generated\.secret/) { secret() }, "UTF-8")
