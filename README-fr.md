radiofeedz
==========

Vos *podcasts* favoris, toujours avec vous dans votre poche.

Avec Radiofeedz vous pouvez avoir accès, télécharger et écouter vos podcasts favoris, dieecrement dans votre poche.

<a href="https://play.google.com/store/apps/details?id=fr.vpm.audiorss">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/fr_app_rgb_wo_60.png" />
</a>

## Comment ça marche ?

Vous ajoutez votre flux RSS favori (un flux de podcasts, de nouvelles, de photos) à Radiofeedz.

Radiofeedz vous liste les dernières nouvelles et podcasts à tout moment, en ligne.

Vous téléchargez vos podcasts à votre appareil Android en quelques clics.

Tous vos podcasts et nouvelles sont dans votre poche, prêts à être écoutés, regardés ou lus, à tout moment, dans le bus, l'avion ou dans votre canapé.

## Comment puis-je trouver ces "flux RSS" ?

### Médias

La plupart (sinon tous) les sites des médias proposent des flux RSS. Il y a généralement un lien sur la page d'accueil vers les flux RSS. Cherchez le mot-clé RSS, le mot-clé podcast, cette icône <img alt="RSS feed icon, from Wikimedia, uploaded by user Anomie" src="https://upload.wikimedia.org/wikipedia/en/thumb/4/43/Feed-icon.svg/128px-Feed-icon.svg.png" height="30px" width="30px" />, ...

Une fois que vous avez trouvé un de ces flux, copiez l'url du flux RSS, puis ouvrez l'application Radiofeedz et cliquez l'icône <img alt="'new' icon from the Icon pack provided by Android" src="/screenshots/ic_action_new.png" height="18px" width="18px" /> en haut à droite de l'écran principal. On vous proposera d'ajouter ce flux. Cliquez sur Oui et appréciez vos podcasts et nouvelles!

<img alt="barre d'action avec l'icône '+'" src="/screenshots/actionbar.png" width="400px" />

### Moteurs de recherche

Il y a plusieurs moteurs de recherche et fournisseurs de flux RSS accessibles sur le Web. Vous pouvez essayer l'un d'eux:

- [Recherche RSS Instantanée](http://ctrlq.org/rss/) vous permet de rechercher les flux RSS des médias existants.
- [Feedzilla](http://www.feedzilla.com/gallery) offre une galerie de flux RSS par catégorie.

Une fois que vous avez choisi un flux RSS parmi ceux-ci, cliquez longuement sur le lien vers le flux RSS, dans votre navigateur Android. Choisissez l'option de partage, puis vous pouvez choisir Radiofeedz. Cela vous redirigera vers l'application, et vous pourrez lire les nouvelles et écouter les podcasts de cette source.

### Exemples

Vous pouvez ajouter des flux de podcasts de la plupart des radios du monde. Prenons l'exemple des radios de Radio France. J'ai développé cette application pour écouter les podcasts de [France Inter][fi], à l'origine. Deux options pour s'abonner aux podcasts des radios de Radio France:
- Si vous disposez de l'application de la radio, ouvrez-la, sur le volet de gauche cliquez sur "Toutes les émissions" puis choisissez votre émission préférée. En haut à droite, il y a une icône ressemblant à une personne émettant des ondes. Cliquez dessus, choisissez l'option "Copier le lien". Ouvrez l'application Radiofeedz, puis cliquez sur l'icône <img alt="barre d'action avec l'icône '+'" src="/screenshots/actionbar.png" width="400px" /> en haut de l'écran. Vous pouvez ainsi ajouter vos podcasts préférés.
- Si vous utilisez votre navigateur sur votre appareil Android, naviguez vers la page de votre émission préférée. Cliquez sur le menu `podcast` puis longuement sur `s'abonner au podcast via RSS`. Un menu d'options apparaît, choisissez `Ouvrir avec une application` puis Radiofeedz. Il vous reste à accepter l'ajout de votre podcast puis l'apprécier!

Par exemple, [voici le lien vers le flux RSS de la chronique Géopolitique de Bernard Guetta][fi-geo]

## J'adore cette appli!

Parfait! Dites-le à vos amis, laissez-les profiter de leurs podcasts eux aussi.

## Il y a des bugs, l'application crashe, qu'est-ce que je fais ?

J'ai testé l'application autant que j'aie pu, mais j'ai probablement oublié des bugs. N'hésitez pas à [ouvrir un problème sur Github][gh-issues] si vous en trouvez. Si c'est le cas, essayez d'être aussi précis que possible (quel flux RSS vous avez utilisé, quel bouton vous avez appuyé, ...). Je pourrai plus facilement corriger les problèmes. En particulier, certains flux peuvent ne pas fonctionner, je n'ai bien entendu pas pu tous les tester.

## Références

[Icône de flux RSS de Wikimedia][rss-wiki], par [Anomie][anomie-wiki]

Cette application dait usage de bibliothèques tierces qui permettent de construire des applications de qualités :
- [Apache Commons Lang (Lang3.0)][lang3] pour les manipulations de flux
- [Gson] pour la désérialisation depuis le format JSON

[gh-issues]: https://github.com/vpmalley/radiofeedz/issues
[rss-icon-wiki]: https://upload.wikimedia.org/wikipedia/en/thumb/4/43/Feed-icon.svg/128px-Feed-icon.svg.png
[rss-wiki]: https://en.wikipedia.org/wiki/File:Feed-icon.svg
[anomie-wiki]: https://en.wikipedia.org/wiki/User:Anomie
[fi]: http://www.franceinter.fr
[fi-geo]: http://radiofrance-podcast.net/podcast09/rss_10009.xml
[serial]: http://serialpodcast.org/
[serial-rss]: http://feeds.serialpodcast.org/serialpodcast
[lang3]: https://commons.apache.org/proper/commons-lang/
[Gson]: https://sites.google.com/site/gson/Home
