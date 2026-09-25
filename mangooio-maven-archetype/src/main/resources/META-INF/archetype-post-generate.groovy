def sep = File.separator
def pool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()   // 62 Zeichen
def rnd = new java.security.SecureRandom()

def secret = (1..64).collect { pool.charAt(rnd.nextInt(pool.length())) }.join()

def path = new File(".").getCanonicalPath() + sep + artifactId + sep +
        "src" + sep + "main" + sep + "resources" + sep + "config.yaml"

def f = new File(path)
f.write(f.getText("UTF-8").replace("application.secret", secret), "UTF-8")