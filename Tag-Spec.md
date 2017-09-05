# NFC Tag Data Specification
The NFC tags which are to be used with our system must be configured to the following specification:

## Data Format
The tag should be formatted with an Ndef data structure, divided into the following entries:

## Entries
### Name
* Type: gatortag/name
* Maximum size: 64 bytes
* Contents: The short-form name of the tag. 
* Size Concerns: If you absolutely need more than 64 characters, then register the long name with the server.

### ID
* Type: gatortag/id
* Size: 64 bytes
* Contents: A unique, random identifier for the tag. Not to be confused with the "Code" section, below; unlike the code, the ID number is *public*, and should be made available on the server.

### Encoding Time
* Type: gatortag/time
* Maximum Size: 48 bytes
* Contents: The unix-time (seconds since 1/1/1970 00:00:00) at which the tag was encoded.
* Size Concerns: If this tag were 32 bytes long, then it would roll over on February 7, 2106. Just to be safe, the size has been set to 48 bytes, which will not roll over until some time in 8,921,556 A.D.

### Code
* Type: gatortag/code
* Size: 512 bytes
* Contents: A random number that is unique to the tag. Unlike the ID number, this number should be *secret*; it is used to prove to the server that a person has been to the tag.
