# Current design goals
## General-purpose
1. Come up with a better name. We will probably never show up in a google search for "nfc tag," so we should probably find something more distinctive
1. Come up with a logo that we can mark the tags with. Something distinctive, but simple enough that somebody can scribble it on an NFC tag with a sharpie

## For basic implementation
#### These are the core software features that have to be made before we have a working system.
1. Design a protocol for creating a unique identifier on an RFID sticker/tag, consisting of:
   1. The tag's immutable serial number
       * Note: this serial number should not be expected to be unique. There are a lot of rfid tags out there, and collisions will occur even without malicious intent.
   1. An amount of plain-text identifying information, such as a short name and placement date for the tag.
       * This should ideally be readable by a smart-phone's normal tag reader, and include a short explanation of what the tag is.
   1. A "code;" that is, an amount of random data, which should be long enough to be immune to brute-force guessing with modern hardware (this will not be very hard, since it's just a random number and not a set of primes).
   
1. Create a server program that:
   1. Allows users to create a profile based on an RSA public-key.
       * Note: The client program (below) should have functionality to generate this key-pair behind the scenes without confusing the user
       * This key should be the basis of the account; i.e., the user signs each request instead of / in addition to using password authentication. This would allow a person's credentials to be relatively independent of the server, and make it easier to facilitate multiple servers running the same software.
   1. Allows users to register an NFC tag (i.e.: serial number, code, plaintext, plus a longer description, approximate location, and other notes that would be too long to fit on the tag itself)
       * The server should generate and a key-pair for each tag. It should make the public-key easily available (and share it with other servers?), but keep the private-key local to the server.
       * Perhaps the server could sign the registering user's key (plus a flag like "creator") with the private key of the tag, associating the tag with the user and allowing the user to perform certain administrative actions (e.g. renaming/deleting the tag, updating the stored serial/code if the tag is destroyed/removed, etc.)
   1. Allows users to upload a serial+code+user hash (see below) as proof that they have scanned a registered tag. This hash should then be verified against one calculated by the server using the registered tag's serial+code and the user's public-key. 
       * If a user provides sufficient proof that he/she has scanned a tag (by serial+code+user hash), then the server should sign the user's public-key with the stored private-key of the tag.
   1. Stores the public-key of registered users, along with a record of signatures made by tags, and provides this information publicly, allowing a user to display which tags they have scanned.
   
1. Create a software application for mobile devices that:
   1. Provides a simple and easy-to-understand interface for interacting with a server by
      * Hiding potentially-confusing operations such as key-pair generation.
      * Displaying a list of tags that the user has scanned, hiding the underlying signatures.
      * Allowing technically-inclined users to use a more detailed configuration.
   1. Allows for the scanning of tags and calculation of a serial+code+user hash without an internet connection, using the following procedure:
      1. Read the serial number and code from the tag.
      1. Combine these numbers with the user's public-key and create a cryptographic hash of the result. Note that this hash will be different for each user.
      1. Store the cryptograhpic hash. Do not store the serial number or the code (this will help to discourage malicious users from simply copying the serial number and code from a tag and sharing it with those who haven't visited the tag, though this is a far cry from actually preventing this behavior (which could only really be done by the "secure mode" detailed below)).
   1. Allows for scanned serial+code+user hashes to be uploaded to a server for verification
   1. Allows for the creation and registration of a tag by the following process:
      1. Prompt the user for the tag's properties (e.g. name, short name, location, notes, etc.).
      1. Read the serial number of the tag.
      1. Write the relevant data (name, creator, and time-stamp) to the tag.
      1. Generate a code and write it to the tag.
      1. Lock the tag to read-only mode (if the tag supports this) in order to prevent any modification by malicious users.
      1. Use the provided information to register the tag with the server.

## Gravy
#### These are extra goals that we can try for once we have a working system.
1. Teams
   1. Allow users to join different "teams." Either a small number of set teams (e.g. when registering a profile, a user can join the Red, Green, Blue, Cyan, Yellow, or Magenta team) or allow users to create and join their own, smaller team. Best used with "CTF mode," below.
   
1. Implement different types of special tags
   1. CTF mode -- A tag in a very public and high-traffic location serves as a "capture point;" some sort of game implemented in the app would allow users (or, more likely, teams) to compete for control of the point.
   1. Sequence mode -- A tag provides a hint for the location of another tag, which provides a hint for the location of another tag. At the end is a tag that can be scanned for a serial+code+user hash. Could be used to guide people around campus, to serve as a "treasure hunt" at a party, etc.
   1. Event mode -- A tag can only be scanned in a certain time range. This one would be easy to implement; just instruct the server to refuse to sign any keys for that tag after a given time.
      * Note that this implementation would make it impossible to run event mode in an offline event, since the tag would expire before the users had the chance to validate.
      * This could be overcome by using "Secure Mode" and including a time-stamp in the signature data.

## Secure Mode
#### This feature would allow for cryptographic verification that a user was actually, definitely *at* a tag (or, at least, somebody with access to the user's public-key was at the tag).
1. Crypto Tags
   1. Some RFID tags are provided specifically for the purpose of cryptograhpy-related operations.
   1. We could create a protocol that would allow certain varieties of these tags to be programmed to sign our users' public-keys; that is:
      * A user uploads their public-key to the tag
      * The tag signs it with the private key that it stores
      * The tag sends the signed public-key back to the user's device
      * The user never has access to the stored private-key, and thus cannot sign any keys that he/she does not have direct access to at the time when he/she is at the tag
1. Problems with this implementation
   1. These tags are not commonly-used, and especially not among the hobbyist community. We'd have to do a lot of research and programming, and because users would have to purchase them direct from factories, they would have to buy in large quantities.

## Full Decentralization
### In the far future, we will want to re-write the server protocol in such a way that it can be easily run as a decentralized network. However, I don't think that anyone currently on the team has the knowledge required to implement it this way at the moment.
