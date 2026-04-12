# javitasnyilvantarto

Fejlesztés alatt álló ékszerjavítás nyilvántartó rendszer backendje.

A szükséges frontend itt érhető el: https://github.com/papp-mate-mark/javitasnyilvantarto-frontend

<h3>Előkövetelmények</h3>

- Java 21 vagy újabb
- [PostgreSQL](https://www.postgresql.org/)

<h3>Környezeti változók</h3>

Az alkalmazás megfelelő futásához és a biztonságos működéshez a következő paramétereket célszerű megadni (környezeti változóként vagy az `application.properties` fájlban):

- `SPRING_DATASOURCE_URL` (pl. `jdbc:postgresql://localhost:5432/jewelrepairstore`)
- `SPRING_DATASOURCE_USERNAME` (adatbázis felhasználónév)
- `SPRING_DATASOURCE_PASSWORD` (adatbázis jelszó)

**Biztonság és hálózat:**

- `APP_CORS_ALLOWED_ORIGINS` (Engedélyezett CORS eredetek vesszővel elválasztva. Alapértelmezett: `http://localhost:4200,http://localhost`)
- `JWT_SECRET_BASE64` (JWT titkos kulcs Base64-ben kódolva. Ha üres, akkor a rendszer generál egy véletlenszerű kulcsot indításkor, de ez nem ajánlott termelési környezetben, mivel újraindításkor érvényteleníti a meglévő tokeneket.)
- `JWT_ACCESS_TOKEN_TTL_MS` (Hozzáférési token lejárati ideje ezredmásodpercben.)
- `JWT_REFRESH_TOKEN_TTL_MS` (Frissítési token lejárati ideje ezredmásodpercben.)

<h3>Fejlesztő/debug szerver indítása</h3>

Lokális fejlesztéshez a szerver elindításához futtassa az alábbi parancsot a projekt gyökérmappájában a Maven Wrapper használatával:

```
mvnw spring-boot:run
```

_Megjegyzés: Első indítás után a rendszer létrehozza a `root` felhasználót, melynek jelszavát a terminálba írja ki._

<h3>Buildelés</h3>

A termelési célú, futtatható `.jar` fájl elkészítéséhez futtassa a következő parancsot:

```
mvnw clean package -DskipTests
```

Ez lefordítja a projektet, és a `target` könyvtárban létrehozza a buildelt változatot. A szervert ezután tetszőleges gépen a következő paranccsal indíthatja el (a pontos fájlnév a `pom.xml` verziójától függ):

```
java -jar target/javitasnyilntarto-0.0.1-SNAPSHOT.jar
```

<h3>Tesztelés</h3>

Az egység- és integrációs tesztek futtatásához használja a következő parancsot:

```
mvnw test
```

<h3>Licenc</h3>

A szoftver kódja oktatási és tanulási célokra használható és módosítható.
Tilos azonban a kódot kereskedelmi célra felhasználni, terjeszteni vagy sajátjaként publikálni a szerző előzetes írásbeli engedélye nélkül.
