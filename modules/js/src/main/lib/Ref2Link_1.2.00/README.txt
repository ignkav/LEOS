PROJECT: Ref2Link
Joinup Release: 1.2.00-EVAL-KIT
Date: 2020-04-17

INTRODUCTION
============

This is a joinup release of Project Ref2Link evaluation kit v1.2.00.
Context information can be found here:
https://ec.europa.eu/isa2/solutions/ref2link_en


NOTICE
======

This evaluation kit is intended to provide a minimal javascript running integration example for the purpose of offline evaluation.
    * It is designed to run on a file system without any network connection.
    * It does not expose most of Ref2Link features both in terms of detection capacity and API scope.
    * Ref2Link v1.2.00 javascript bundle included in this package is distributed under a EUPL v.1.2 licence.
	  This bundle contains the necessary logic for EU and some Member states law detection in 24 EU national languages.
    * Ref2Link is still under development, so that some features may be added, removed or changed over course of time.
    * Users having access to the TESTA network can use the more advanced 'LinkPad' Ref2Link frontend: https://webgate.ec.testa.eu/ref2link/.
    * Users having an EU Login account can consult the support WIKI: https://webgate.ec.europa.eu/fpfis/wikis/spaces/viewspace.action?key=Ref2Link.


DEMO
====

Unzip the package on a file system location.
Open local file 'demo.html' with your internet browser.
The demo consists in two text areas:
    * A left-side area where raw text can be typed or pasted.
    * A right-side area where the same text is displayed upon processing ('parse >>' button).
    * At first launch, a sample of text is automatically loaded and processed.

NB: the evaluation kit has been tested with recent versions of the following internet browsers: Chrome, Firefox, Ms Edge, Ms IE.
Depending on your configuration, restrictions might apply to direct opening of local html files.
In that case, it is still possible to copy the files under an http: listener.


CONTACT
=======

Any request, question or comment can be sent to:
SJ-REF2LINK-SUPPORT@ec.europa.eu