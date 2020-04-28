Random random = new Random(System.currentTimeMillis())
def sep = File.separator
def pool = ['a'..'z','A'..'Z',0..9].flatten()
def path = new File(".").getCanonicalPath() + sep + artifactId + sep + "src" + sep + "main" + sep + "resources" + sep + "config.props"
def replacePatternInFile(file, Closure replaceText) {
    file.write(replaceText(file.text))
}

key = (1..64).collect { pool[random.nextInt(pool.size())] }
replacePatternInFile(new File(path)){
    it.replaceAll("application.secret", key.join())
}

key = (1..64).collect { pool[random.nextInt(pool.size())] }
replacePatternInFile(new File(path)){
    it.replaceAll("session.cookie.secret", key.join())
}

key = (1..64).collect { pool[random.nextInt(pool.size())] }
replacePatternInFile(new File(path)){
    it.replaceAll("authentication.cookie.secret", key.join())
}


key = (1..64).collect { pool[random.nextInt(pool.size())] }
replacePatternInFile(new File(path)){
    it.replaceAll("flash.cookie.secret", key.join())
}