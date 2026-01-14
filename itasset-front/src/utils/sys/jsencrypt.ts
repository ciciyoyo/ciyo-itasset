// @ts-ignore
import JSEncrypt from 'jsencrypt'

// 密钥对生成 http://web.chacuo.net/netrsakeypair

const publicKey =
    'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKxUqe3/UXK3sDtTMD8q6XNWvVITnZxg\n' +
    'GYruNoWSXrvvkGfgW/Ss8BfXThRMAL/miIRq8G6+1xEXTBTEdg/DBKECAwEAAQ=='

const privateKey =
    'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEArFSp7f9RcrewO1Mw\n' +
    'Pyrpc1a9UhOdnGAZiu42hZJeu++QZ+Bb9KzwF9dOFEwAv+aIhGrwbr7XERdMFMR2\n' +
    'D8MEoQIDAQABAkBtUSOegtjC4b0cVPAFEg6XCM39082mVdu+ItBZOPl5Uzo9BHRD\n' +
    'xnr5oTkcCRDB+mZELkioeUFa/uqivTaqzDkBAiEA441k+Votl6zXaez6kV9uVXQC\n' +
    'fBKPgOEAZFTP603p9LECIQDB3/O4UbtWLQN1Tf6Tj19zGI7AS0BaEmgw6cx4bnDK\n' +
    '8QIgavOjOPvkn/ySBuxmXPuArVNoc455unaGq6GdVBh71RECIHqSdmn/8mrHRrpx\n' +
    'NxRfvr7rtcTJTsQjgw/5oLY7TMBhAiEA2zk34VyWtKPRUdfFWAHjQoyQ2DJtqvs4\n' +
    'xrYzu/CSTbU='

// 加密
export function encrypt(txt: string) {
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(publicKey) // 设置公钥
    return encryptor.encrypt(txt) // 对数据进行加密
}

// 解密
export function decrypt(txt: string) {
    const encryptor = new JSEncrypt()
    encryptor.setPrivateKey(privateKey) // 设置私钥
    return encryptor.decrypt(txt) // 对数据进行解密
}
