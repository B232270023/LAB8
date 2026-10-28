# Lab 08 — Mockito mock объектууд

[![Maven tests](https://github.com/B232270023/LAB8/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/B232270023/LAB8/actions/workflows/test.yml)

## Автомат CI

GitHub Actions дахь `Maven tests` workflow нь `main` руу push хийх болон `main` руу чиглэсэн pull request бүрд Maven тестийг ажиллуулна. Ажлын орчин Temurin Java 17 ашиглаж, `mvn --batch-mode --no-transfer-progress test` командаар тестүүдийг ажиллуулдаг. Дээрх badge нь сүүлийн CI үр дүнг харуулна.

## Оюутан ба ажиллуулах орчин

- Нэр: О.Шинэбаяр
- Оюутны код: B232270023

`java -version`:

```text
openjdk version "17.0.20.1" 2026-08-18
OpenJDK Runtime Environment Temurin-17.0.20.1+1 (build 17.0.20.1+1)
OpenJDK 64-Bit Server VM Temurin-17.0.20.1+1 (build 17.0.20.1+1, mixed mode, sharing)
```

`mvn -version`:

```text
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: C:\Users\newtech\Documents\Codex\2026-10-28\la\work\maven\apache-maven-3.9.11
Java version: 17.0.20.1, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

## Хэсэг A — Mockito тестүүд

`CourseRegistrationServiceTest` нь `StudentRepository` болон `EmailSender`-ийг `@Mock` объектуудаар орлуулж, үйлчилгээний логикийг тусгаарлан шалгана. A1–A7 нь амжилттай бүртгэл, олдоогүй/идэвхгүй оюутан, дутуу prerequisite, имэйл илгээлтийн exception, хоёр төрлийн `null` оролт, мөн имэйлийн body дахь хичээлийн кодыг шалгана. A8 нь `@ParameterizedTest` ба `@CsvSource` ашиглан гурван бүртгэлийн алдааны үр дүнг шалгана.

A8-ийн мөр бүр өөр салбарын stub ашигладаг тул зарим мөрөнд бусад stub дуудагдахгүй үлддэг. Mockito-ийн strict stubs энэ тохиолдлыг unnecessary stubbing гэж үзэхээс сэргийлж, зөвхөн A8-д зориулсан stub-уудыг `lenient()` болгосон.

`results/mvn-test.txt`-ийн эцсийн гаралт:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Хэсэг B — Mutation шалгалт

Мутант бүрийн дараа эх кодыг сэргээж, эцэст нь `mvn clean test` ажиллуулсан. Дараах унасан тестүүд нь `results/mvn-test-mutant-1..3.txt` файлуудтай таарна.

| Мутант | Зориудын өөрчлөлт | Унасан тест(үүд) | Үр дүн |
|---|---|---|---|
| 1 | Идэвхгүй оюутны шалгалтыг авах | `inactiveStudent_registersRejected_doesNotInteractWithEmailSender` | 1 Failure, BUILD FAILURE |
| 2 | `saveRegistration(...)` дуудалтыг авах | `activeStudent_registersSuccessfully_savesAndSendsEmail`; `emailSenderThrows_registrationStillSucceedsAndIsSaved` | 2 Failures, BUILD FAILURE |
| 3 | `courseId == null` шалгалтыг авах | `nullInput_registerThrowsWithoutInteractions` | 1 Failure, BUILD FAILURE |

Эдгээр мутацыг бүгдийг тестүүд барьсан. Эцсийн `src/main` код сэргээгдсэн бөгөөд эцсийн `results/mvn-test.txt` 10 тест амжилттай болсныг харуулна.

## Дүгнэлт

Mockito mock нь framework-ээр тохируулж, дуудалтыг нь шалгадаг объект бөгөөд fake нь интерфейсийг бодитоор хэрэгжүүлсэн хялбар хувилбар юм. Mock нь repository, SMTP зэрэг гаднын хамаарлын үр дүн ба үйлдлийг тусгаарлан хянахад тохиромжтой. Fake нь бодит ажиллагааны энгийн хувилбар хэрэгтэй үед илүү ойлгомжтой байж болно. `Student` шиг өгөгдлийн объект, энгийн коллекц болон шалгаж буй бизнес дүрмийг өөрийг нь mock хийх шаардлагагүй. Ийм объектыг mock болговол тест бодит өгөгдөл дээр логик ажиллаж байгааг шалгахгүй болох эрсдэлтэй. `verify`-г хэт олноор хэрэглэх нь тестийг хэрэглэгчид харагдах үр дүнгээс илүү дотоод хэрэгжүүлэлтийн дараалалтай холбож, кодын жижиг өөрчлөлтөд хэврэг болгодог. Тиймээс энэ лабораторид хадгалалт, имэйл зэрэг чухал side effect-ийг л шалгаж, бизнес үр дүнг assertion-аар баталсан.
