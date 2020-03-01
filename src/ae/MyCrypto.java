package ae;
/*
Криптографические утилиты, по мотивам
http://findevelop.blogspot.ru/2013/04/java.html
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

public class MyCrypto {
  private final String  ALGORITHM = "RSA";
  private final int     ALGORITHM_LENGTH = 128; // длина RSA шифровки (байт)
  private final String  ALGORITHM_SIMMETRIC = "AES";
  private final int     ALGORITHM_SIMMETRIC_LENGTH = 16; // длина ключа AES (байт)
  private final String  HASH_METHOD = "MD5";
  private final int     HASH_LENGTH = 16;  // длина хэш суммы (байт)

  private String s_publicKey = null;
  private String s_privateKey = null;

  private PublicKey k_publicKey = null;
  private PrivateKey k_privateKey = null;

  public MyCrypto() {

  }

  public MyCrypto(String publicKey, String privateKey) {
    this.s_publicKey  = publicKey;
    this.s_privateKey = privateKey;
    try {
      if (publicKey != null && publicKey.length() > 4)
        k_publicKey = restorePublic(s_publicKey);     // восстановим публичный ключ
      if (privateKey != null && privateKey.length() > 4)
        k_privateKey = restorePrivate(s_privateKey);  // восстановим приватный ключ
    } catch (Exception e) {
      System.err.println("?-Error-ошибка восстановления публичного или приватного ключа: " + e.getMessage());
    }
  }

  /**
   * Сгенерировать новую пару ключей
   */
  public void generateKeys() {
    try {
      final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
      keyGen.initialize(1024, new SecureRandom());
      final KeyPair key = keyGen.generateKeyPair();
      this.k_publicKey = key.getPublic();
      this.s_publicKey = byte2Hex(k_publicKey.getEncoded());
      this.k_privateKey = key.getPrivate();
      this.s_privateKey = byte2Hex(k_privateKey.getEncoded());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getPrivateKey() {
    return s_privateKey;
  }

  public String getPublicKey() {
    return s_publicKey;
  }

  /**
   * Зашифровать сообщение для публичным ключом.
   * Зашифровываем сообщения публичным ключом Получателя Сообщения
   *
   * @param message сообщение
   * @return зашифрованная строка из 16-ричных символов
   */
  public String encrypt(String message) {
    String otv = "<error encrypt>";
    try {
      byte[] cipherText = encryptRSA(message.getBytes());
      otv = byte2Hex(cipherText);
    } catch (GeneralSecurityException ex) {
      //eх.printStackTrace();
      otv = otv + " " + ex.getMessage();
    }
    return otv;
  }

  /**
   * Зашифровать байты по алгоритму RSA
   * @param mess  исходное сообщение
   * @return  зашифрованное сообщение
   */
  private byte[] encryptRSA(byte[] mess) throws GeneralSecurityException
  {
    final Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, this.k_publicKey);
    byte[] cipherText = cipher.doFinal(mess);
    return cipherText;
  }

  /**
   * Зашифровать байты по алгоритму AES
   * @param key   ключ
   * @param mess  исходное сообщение
   * @return  зашифрованное сообщение
   * @throws GeneralSecurityException
   */
  protected byte[] encryptAES(byte[] key, byte[] mess) throws GeneralSecurityException
  {
    final Cipher cipher = Cipher.getInstance(ALGORITHM_SIMMETRIC);
    SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM_SIMMETRIC);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] cipherText = cipher.doFinal(mess);
    return cipherText;
  }

  /**
   * Расшифровать зашифрованное сообщение hex (16-ричных) символов.
   * Символы с кодом менее '0' (пробел, табуляция, перевод строки) игнорируются
   * Расшифровываем приватных ключом Получателя Сообщенияю
   * @param cryptMessage зашифрованное сообщение из hex символов и возможно пробелов, табуляций, переводов строки
   * @return расшифрованное сообщение
   */
  public String decrypt(String cryptMessage)
  {
    String otv = "<error decrypt> "; // ответ в случае ошибки раскодирования
    try {
      String hexc = onlyHex(cryptMessage);    // зашифрованное (16-ричные символы)
      byte[] b = hex2Byte(hexc);              // зашифрованное байты
      byte[] dectyptedText = decryptRSA(b);
      otv = new String(dectyptedText);
    } catch (GeneralSecurityException ex) {
      //ex.printStackTrace();
      otv = otv + ex.getMessage();
    }
    return otv;
  }

  /**
   * Расшифровать сообщения по RSA
   * @param cryptMess  зашифрованные байты
   * @return  расшифрованные байты сообщения
   */
  private byte[]  decryptRSA(byte[] cryptMess) throws GeneralSecurityException
  {
    final Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, this.k_privateKey);
    byte[] dectyptedText = cipher.doFinal(cryptMess);
    return dectyptedText;
  }

  /**
   * Расшифровать сообщения по AES
   * @param key       ключевые байты
   * @param cryptMess зашифрованные байты
   * @return  расшифрованные байты сообщения
   */
  private byte[]  decryptAES(byte[] key,byte[] cryptMess) throws GeneralSecurityException
  {
    final Cipher cipher = Cipher.getInstance(ALGORITHM_SIMMETRIC);
    SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM_SIMMETRIC);
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    byte[] dectyptedText = cipher.doFinal(cryptMess);
    return dectyptedText;
  }

  /**
   * Восстановить публичный ключ из строки hex (16-ричных символов)
   * @param hexStr  строка hex(16-ричных) символов
   * @return  публичный ключ
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  protected PublicKey restorePublic(String hexStr) throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
    byte[] b = hex2Byte(hexStr);    // сделаем, требуемый байтовый массив
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b);
    return keyFactory.generatePublic(publicKeySpec);
  }

  /**
   * Восстановить приватный ключ из строки hex (16-ричных символов)
   * @param hexStr  строка hex(16-ричных) символов
   * @return  приватный ключ
   */
  private PrivateKey restorePrivate(String hexStr) throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
    byte[] b = hex2Byte(hexStr);    // сделаем, требуемый байтовый массив
    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(b);
    return keyFactory.generatePrivate(privateKeySpec);
  }

  /*
  private String byte2Hex(byte b[])
  {
    String hs = "";
    String stmp;
    for (int n = 0; n < b.length; n++) {
      stmp = Integer.toHexString(b[n] & 0xff);
      if (stmp.length() == 1)
        hs = hs + "0" + stmp;
      else
        hs = hs + stmp;
    }
    return hs.toLowerCase();
  }

  private byte hex2Byte(char a1, char a2)
  {
    int k;
    if (a1 >= '0' && a1 <= '9') k = a1 - 48;
    else if (a1 >= 'a' && a1 <= 'f') k = (a1 - 97) + 10;
    else if (a1 >= 'A' && a1 <= 'F') k = (a1 - 65) + 10;
    else k = 0;
    k <<= 4;
    if (a2 >= '0' && a2 <= '9') k += a2 - 48;
    else if (a2 >= 'a' && a2 <= 'f') k += (a2 - 97) + 10;
    else if (a2 >= 'A' && a2 <= 'F') k += (a2 - 65) + 10;
    else k += 0;
    return (byte) (k & 0xff);
  }

  private byte[] hex2Byte(String str)
  {
    int l = str.length();
    if (l % 2 != 0) return null;
    byte r[] = new byte[l / 2];
    int k = 0;
    for (int i = 0; i < str.length() - 1; i += 2) {
      r[k] = hex2Byte(str.charAt(i), str.charAt(i + 1));
      k++;
    }
    return r;
  }
*/

  private String byte2Hex(byte[] b)
  {
    String hex = "";
    try {
      hex = DatatypeConverter.printHexBinary(b);
    } catch (IllegalArgumentException ex) {
      System.err.println(ex.getMessage());
    }
    return hex;
  }

  protected byte[] hex2Byte(String str)
  {
    byte[] b = new byte[1];
    try {
      b = DatatypeConverter.parseHexBinary(str);
    } catch (IllegalArgumentException ex) {
      System.err.println(ex.getMessage());
    }
    return b;
  }

  // TODO сделать проверку хэш-суммы сообщения
  // CRC32 https://www.quickprogrammingtips.com/java/how-to-calculate-crc32-checksum-in-java.html
  // http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
  // https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html

  /**
   * Сделать хэш-сумму сообщения
   * @param b байты сообщения
   * @return  байты хэш-суммы
   */
  private byte[]  getHash(byte[] b)
  {
    byte[] hashedBytes;
    try {
      MessageDigest digest = MessageDigest.getInstance(HASH_METHOD);
      digest.update(b);
      hashedBytes = digest.digest();
    } catch (NoSuchAlgorithmException ex) {
      System.err.println(ex.getMessage());
      hashedBytes = new byte[HASH_LENGTH];  // неправильный хэш
    }
    return hashedBytes;
  }

  /**
   * Сверить хэш сумму сообщения и заданного хэша
   * @param b     байты сообщения
   * @param hash  байты хэша
   * @return  true - совпадает, false - не совпадает
   */
  private boolean checkHash(byte[] b, byte[] hash)
  {
    byte[] hashedBytes = getHash(b);
    int l = hashedBytes.length;
    if(l != hash.length)
      return false;
    for (int i = 0; i < l; i++) {
      if(hash[i] != hashedBytes[i])
        return false;
    }
    return true;
  }

  /**
   * Добавляет переводы строки во входную строку
   * @param input входная строка
   * @return  выходная строка с переводами строк
   */
  private String wrapLine(String input)
  {
    final int mmx = 44; // максимальная длина строки
    final int n = input.length();
    final int m = n + 2*(mmx-1+n)/mmx; // добавка перевод строк
    int i;
    StringBuilder sb = new StringBuilder(m);
    for(i=0; i<n;) {
      sb.append(input.charAt(i++));
      if((i % mmx) == 0)
        sb.append("\r\n");  // добавляем перевод строки
    }
    return sb.toString();
  }

  /**
   * Выбирает из входной строки только hex-символы и записывает их на выход
   * @param input входная строка
   * @return  выходная строка состоящая только их hex-символов
   */
  public String onlyHex(String input)
  {
    int i, n;
    n = input.length();
    StringBuilder sb = new StringBuilder(n);
    for(i=0; i<n; i++) {
      char c = input.charAt(i);
      if( (c >= '0' && c <= '9') ||
          (c >= 'a' && c <= 'f') ||
          (c >= 'A' && c <= 'F') )
        sb.append(c);
    }
    return sb.toString();
  }

  ////////////////////////////////////////////////
  // Эксперимент с большими данными

  /**
   * Зашифровать большие данные сеансовым ключом, который шифруется открытым ключом
   * [сеансовый ключ] 128 байт
   * [хэш(MD5)+сообщение]
   * @param mess  входные данные
   * @return  зашифрованные данные
   */
  public byte[] encryptBigData(byte[] mess)
  {
    byte[] otvet;     // ответ - зашифрованные данные
    byte[] hash_mess; // рабочий буфер
    // подготовим сообщение к шифрованию, добавим в начало хэш-сумму
    try {
      // mess - байты сообщения
      byte[] hash = getHash(mess);    // получим HASH_LENGTH байт хэш-суммы сообщения
      // создадим выходной поток, в которой пеместим хэш и текст сообщения
      ByteArrayOutputStream bom = new ByteArrayOutputStream(hash.length + mess.length);
      // [хэш-сумма]
      // [сообщение]
      bom.write(hash);
      bom.write(mess);
      hash_mess = bom.toByteArray();  // массив хэш + сообщение
    } catch (IOException ex) {
      System.err.println("?-Error-encryptBigData(): " + ex.getMessage());
      return null;
    }
    // случайный ключ сеанса
    SecureRandom random = new SecureRandom();
    byte[] seskey = new byte[ALGORITHM_SIMMETRIC_LENGTH];  // формируем сеансовый ключ
    random.nextBytes(seskey);      // случайная последовательность
    try {
      byte[] cryptSesKey = encryptRSA(seskey);  // зашифруем ключ - получим 128 байт
      byte[] cryptText   = encryptAES(seskey, hash_mess); // шифруем спец-сообщение
      // создадим выходной поток, в которой пеместим зашифрованный ключ и сообщение с хэш-суммой
      ByteArrayOutputStream bos = new ByteArrayOutputStream(cryptSesKey.length + cryptText.length);
      // [зашифрованный RSA сеансовый ключ]
      // [зашифрованное AES хэш сумма + сообщение]
      bos.write(cryptSesKey);
      bos.write(cryptText);
      otvet = bos.toByteArray();
    } catch (IOException | GeneralSecurityException ex) {
      //eх.printStackTrace();
      System.err.println("?-Error-encryptBigData(): " + ex.getMessage());
      return null;
    }
    return otvet;
  }

  /**
   * Зашифровать большие входные данные сеансовым ключом, который шифруется
   * открытым ключом в зашифрованный текст.
   * [сеансовый ключ] 128 байт
   * [хэш(MD5)+сообщение]
   * @param inputData  входные данные
   * @return  зашифрованное текстовое сообщение
   */
  public String encryptText(byte[] inputData)
  {
    byte[] crypt_mess = encryptBigData(inputData);
    if(crypt_mess == null)
      return "<encrypt error>";
    String res = byte2Hex(crypt_mess);    // перевести в HEX
    String otvet = wrapLine(res);         // разбить на строки
    return otvet;
  }

  /**
   * Зашифровать большое текстовое сообщение сеансовым ключом, который шифруется
   * открытым ключом в зашифрованный текст.
   * [сеансовый ключ] 128 байт
   * [хэш(MD5)+сообщение]
   * @param text  входное сообщение
   * @return  зашифрованное текстовое сообщение
   */
  public String encryptText(String text)
  {
    byte[] bt = text.getBytes();
    String otvet = encryptText(bt);
    return otvet;
  }

  /**
   * Расшифровать большие данные. Расшифровав сеансовый ключ приватным ключом,
   * а затем расшифровав хэш-сумму(16 байт) + сообщение. После чего, сообщение проверить
   * на совпадение хэш-суммы (MD5).
   * @param cryptoData зашифрованные данные
   * @return
   */
  public byte[] decryptBigData(byte[] cryptoData)
  {
    int l, lk, i, j;
    byte[] mess;          // расшифрованное сообщение
    byte[] decrHashMess;
    try {
      l = cryptoData.length;      // длина общего массивы
      lk = ALGORITHM_LENGTH;      // длина массива зашифрованного ключа
      //
      byte[] cryptSesKey = new byte[lk];            // байтовый массив с зашифрованным ключом
      byte[] cryptText = new byte[l - lk];          // байтовый массив с зашифрованным сообщением
      for(i=0; i < lk; i++) cryptSesKey[i] = cryptoData[i];
      for(j=0; i < l;)      cryptText[j++] = cryptoData[i++];
      byte[] sesKey = decryptRSA(cryptSesKey);      // расшифрованный ключ
      decrHashMess  = decryptAES(sesKey, cryptText); // расшифрованное сообщение hash + mess
    } catch (NullPointerException | NegativeArraySizeException| ArrayIndexOutOfBoundsException | GeneralSecurityException ex) {
      System.err.println("?-Error-decryptBigData(): " + ex.getMessage());
      return null;
    }
    // расшифровали хэш + сообщение проверим сообщение по хэшу
    try {
      l = decrHashMess.length;  // длина хэш+сообщение
      lk = HASH_LENGTH;         // длина хэш-суммы
      byte[] hash = new byte[HASH_LENGTH];
      mess = new byte[l-lk];
      for(i=0; i < lk; i++) hash[i] = decrHashMess[i];    // хэш-сумма
      for(j=0; i < l;)    mess[j++] = decrHashMess[i++];  // сообщение
      boolean ok = checkHash(mess, hash); // проверим хэш сумму сообщения
      if(!ok) {
        System.err.println("?-Error-decryptBigData(): ошибка контрольной суммы");
        return null;
      }
    } catch (NegativeArraySizeException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
      System.err.println("?-Error-decryptBigData(): " + ex.getMessage());
      return null;
    }
    return mess;
  }

  /**
   * Расшифровать большое сообщение. Расшифровав сеансовый ключ приватным ключом,
   * а затем расшифровав хэш-сумму(16 байт) + сообщение. После чего, сообщение проверить
   * на совпадение хэш-суммы (MD5).
   * @param message зашифрованное соообщение
   * @return
   */
  public String decryptText(String message)
  {
    String otvet = "<error decrypt> "; // ответ в случае ошибки раскодирования
    try {
      String mess     = onlyHex(message);       // hex символы
      byte[] crypto   = hex2Byte(mess);         // зашифрованные байты
      byte[] decrypt  = decryptBigData(crypto); // расшифрованные байты
      if(decrypt != null)
        otvet = new String(decrypt);            // расшифрованный текст
    } catch (Exception ex) {
      System.out.println("?-Error-decryptText(): " + ex.getMessage());
    }
    return otvet;
  }

} // end of class.

