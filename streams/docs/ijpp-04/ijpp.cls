% File name: ijpp.cls 
% Created by Ming Kin Lai by tweaking llncs.cls  Nov 2003
% This is an UNOFFICIAL Latex class file for use in publishing with 
% Int'l Journal of Parallel Programming.  
% It has two modes: draft and final.
% The draft mode prints the document in a format that is as close as 
% I can get to that required by the IJPP publisher in submitting the
% paper, i.e. double-spaced, Times Roman font size 12, all figures
% and tables put at the end, citation in superscript within parenthesis,
% etc.
% Use of the draft mode depends on the use of the endfloat package,
% which generates a .ttt and a .fff file.
% The final mode prints the document that is about 80% like what
% would actually appear in the journal.  You are not supposed to 
% submit your paper in this format to the publisher; this mode is
% just for the purpose of allowing you to see how your paper would
% probably look like when it is actually printed in the journal. 
% Or you may format your paper using the final mode when you send
% it for reviewers' review.
% The draft mode uses 12 point font whereas the final mode uses 11 point.
% You may need to print your figures and tables using the final mode
% and submit them together with the main body of the paper (in 11 point)
% to the publisher.
% There are some other options available in this class but they are
% just inherited from the LNCS class file; so ignore them.
% There is an endfloat.cfg file that should come with this file.
% endfloats.cfg is to configure the endfloat package so that we
% can get what the draft mode calls for.
% A BIBTEX style file is also avaialble to mimic IJPP's 
% bibliography style.
%
% An example to use this cls file:
%
%  %%%------- final mode --------
%  %\documentclass[final]{ijpp}
%  %\usepackage[scaled=.90]{helvet}  % closer to the font used by the title
%  %for final mode, need to manually turn off \doublespacing in the
%  %bibliography section below
%  %%%------- draft mode --------
%  \documentclass[draft]{ijpp}
%
%  %use the doublespace, endfloat and endfloatcfg styles if draft mode is chosen
%  \usepackage[doublespacing]{setspace}  % if your version is below 6.7, 
%                                          use the \doublespacing macro
%  % The alternative is to \renewcommand{\baselinestretch}{1.7} but
%  %  it does not make the text (or entries) in the list of
%  %  figures/tables double-spaced (at least I am not able to work it out);
%  %  and it also makes the text in a float double-spaced, which is undesirable
%  %for draft mode, also need to manually turn on \doublespacing in the
%  %bibliography section below
%  \usepackage[noheads,tablesfirst]{endfloat}
%
%  \usepackage[final,dvips]{graphicx}
%  \usepackage{latexsym}
%  \usepackage{mathptmx} % must be placed after \usepackage{latexsym}
%  \usepackage{url}
%
%  \begin{document}
%
%  \title{Example {\LaTeX} File for IJPP}
%  \titlerunning{Example {\LaTeX} File for IJPP}
%  \author{Ming Kin Lai,\inst{1} John Doe,\inst{2}
%     and Joe Smith\inst{1}
%  }
%  \institute{School of Computer Science,
%     So-and-so University,
%     Anytown, Anystate 12345-6789.
%     Email: \{lai,smith\}\@any.edu
%    \and
%     Parallel Computing Lab,
%     University of Such-and-such,
%     123 Main St., Hong Kong.
%     Email: doe\textit{@}any.edu
%  }
%  \authorrunning{Lai, Doe, and Smith}
%
%  \maketitle
%  \bibliographystyle{ijpp}   % the ijpp.bst bibliography style
%
%  \begin{abstract}
%    The abstract goes here.  The keywords below should be separated
%    by semi-colons, and ended with a period.
%  \end{abstract}
%  \begin{keywords}
%    Parallel programming; distributed computing.
%  \end{keywords}
% 
%  \section{Introduction}
%  \label{sect:intro}  
%    Parallel programming is important,\cite{Lai:WPP:2003,Lai:PPV:2003} and
%    distributed programming is also important.\cite{Lai:IDP:2003} In
%    Ref.~1, a great deal of examples are given.
%     
%    The footnote numbering continues the institution numbering.
%  \subsection{Parallel Programming}
%  \label{sect:pp}
%    You should write ``Section 1'', not ``section 1''.  For example, the
%    conclusion is in Section~\ref{sect:concl}.  
%    You should spell out ``Figure'' at the beginning of a sentence,
%    but abbreviate it to ``Fig.'' in the middle of a sentence.
%    So, write ``Figure~1 illustrates that ...'', but ``As shown in
%    Fig.~1 ...''.
%    
%    Capitalize each letter of each word in Section heading; capitalize
%    the first letter of each significant word in Subsection and Subsubsection
%    headings.
%    
%  \subsubsection{Parallel Programming and its Details}     
%  \label{sect:pps}    
%    IJPP's web site is \url{http://www.kluweronline.com/issn/0885-7458/}.
%  \section{Conclusion}
%  \label{sect:concl}
%    Use \textbackslash usepackage[final]\{graphicx\} 
%    instead of \textbackslash usepackage\{graphicx\} to make sure
%    the eps figures show up.
%    Good luck!\footnote{Again, good luck!}
%  \section*{ACKNOWLEDGMENTS}
%    The research has been supported by M. K. Lai Foundation Grant 1234
%    (Ming Kin Lai), Lai \& Lai Foundation Grant 5678 (John Doe)
%    and NSF Grant 789 (Joe Smith).  Any opinions, findings, and
%    conclusions or recommendations expressed in
%    this paper are those of the authors and do not necessarily reflect
%    the views of the NSF.  The authors thank all the members of the
%    Distributed System Research Group at So-and-so University who were
%    engaged in the XYZ project.  The authors gratefully acknowledge
%    useful discussions with Mary Smith.  We also thank the anonymous
%    reviewers for their comments.  Preliminary versions of this research
%    appeared in the Supercomputing Symposium 2003.  The experiments were
%    conducted with resources provided by NCSA at University of Such-and-such.
%
%  % ---- Bibliography ----
%  %if draft mode, need insert doublespacing manually here:
%  \doublespacing
%  \bibliography{bibfile}
%  % Note: When you submit your LaTeX source file, you may want to
%  % comment out the \bibliography{bibfile}
%  % line and copy and paste the content of the .bbl file in its place.
%
%  \clearpage
%  \end{document}
%
% End of example
%
% Change history:
%   Version 1.01 3/15/2004:
%   Keep the margin of the [final] mode unchanged, but add new code so that the
%   margin of the [draft] mode is 1 inch.
% 
% This file is provided "as is" without warranty of any kind, 
% either express or implied.
% You are welcome to send me a note of appreciation if you find this
% file useful.
%
%   Ming Kin Lai minglai@hotmail.com
%
%
%
% LLNCS DOCUMENT CLASS -- version 2.13 (28-Jan-2002)
% Springer Verlag LaTeX2e support for Lecture Notes in Computer Science
%
%%
%% \CharacterTable
%%  {Upper-case    \A\B\C\D\E\F\G\H\I\J\K\L\M\N\O\P\Q\R\S\T\U\V\W\X\Y\Z
%%   Lower-case    \a\b\c\d\e\f\g\h\i\j\k\l\m\n\o\p\q\r\s\t\u\v\w\x\y\z
%%   Digits        \0\1\2\3\4\5\6\7\8\9
%%   Exclamation   \!     Double quote  \"     Hash (number) \#
%%   Dollar        \$     Percent       \%     Ampersand     \&
%%   Acute accent  \'     Left paren    \(     Right paren   \)
%%   Asterisk      \*     Plus          \+     Comma         \,
%%   Minus         \-     Point         \.     Solidus       \/
%%   Colon         \:     Semicolon     \;     Less than     \<
%%   Equals        \=     Greater than  \>     Question mark \?
%%   Commercial at \@     Left bracket  \[     Backslash     \\
%%   Right bracket \]     Circumflex    \^     Underscore    \_
%%   Grave accent  \`     Left brace    \{     Vertical bar  \|
%%   Right brace   \}     Tilde         \~}
%%
\NeedsTeXFormat{LaTeX2e}[1995/12/01]
%\ProvidesClass{llncs}[2002/01/28 v2.13
%\ProvidesClass{ijpp}[2003/11/14 v1.0
\ProvidesClass{ijpp}[2004/3/15 v1.01
%^^J LaTeX document class for Lecture Notes in Computer Science]
^^J LaTeX document class for Int'l Journal of Parallel Programming]
% Options
\let\if@envcntreset\iffalse
\DeclareOption{envcountreset}{\let\if@envcntreset\iftrue}
\DeclareOption{citeauthoryear}{\let\citeauthoryear=Y}
\DeclareOption{oribibl}{\let\oribibl=Y}
\let\if@custvec\iftrue
\DeclareOption{orivec}{\let\if@custvec\iffalse}
\let\if@envcntsame\iffalse
\DeclareOption{envcountsame}{\let\if@envcntsame\iftrue}
\let\if@envcntsect\iffalse
\DeclareOption{envcountsect}{\let\if@envcntsect\iftrue}
\let\if@runhead\iffalse
\DeclareOption{runningheads}{\let\if@runhead\iftrue}

\let\if@openbib\iffalse
\DeclareOption{openbib}{\let\if@openbib\iftrue}

% draft & final modes added by Ming Kin Lai - begin
\newif\if@draftmode       \global\@draftmodefalse
% draft mode prints everything in double-spaced Times Roman 
% and puts figures and tables at the end
\DeclareOption{draft}{\let\if@runhead\iftrue \global\@draftmodetrue} 
% final mode prints titles, authors, and headings in special fonts
\DeclareOption{final}{\let\if@runhead\iftrue \global\@draftmodefalse}
% draft & final modes added by Ming Kin Lai - end

% languages
\let\switcht@@therlang\relax
\def\ds@deutsch{\def\switcht@@therlang{\switcht@deutsch}}
\def\ds@francais{\def\switcht@@therlang{\switcht@francais}}

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}

\ProcessOptions

%\LoadClass[twoside]{article}  %LNCS
\if@draftmode
  \LoadClass[twoside,12pt]{article}
\else
  \LoadClass[twoside,11pt]{article}
\fi
\RequirePackage{multicol} % needed for the list of participants, index

% added or changed by MKL - begin
\linespread{0.88}
\if@draftmode
  \setlength{\topmargin}{45pt}  % approx 1 inch
  \setlength{\textwidth}{16.4cm}  % approx 1 inch on left and right each 
  \setlength{\textheight}{21.7cm}  % approx 1 inch on top and bottom each
\else
  \setlength{\topmargin}{92pt}  
  \setlength{\textwidth}{11.4cm}  %LNCS: 12.1 
  \setlength{\textheight}{17.6cm}  %LNCS: 19.3 
\fi
%\renewcommand{\thepage}{\textsf{\page}}
% added or changed by MKL - end
\renewcommand\@pnumwidth{2em}
\renewcommand\@tocrmarg{3.5em}
%
\def\@dottedtocline#1#2#3#4#5{%
  \ifnum #1>\c@tocdepth \else
    \vskip \z@ \@plus.2\p@
    {\leftskip #2\relax \rightskip \@tocrmarg \advance\rightskip by 0pt plus 2cm
               \parfillskip -\rightskip \pretolerance=10000
     \parindent #2\relax\@afterindenttrue
     \interlinepenalty\@M
     \leavevmode
     \@tempdima #3\relax
     \advance\leftskip \@tempdima \null\nobreak\hskip -\leftskip
     {#4}\nobreak
     \leaders\hbox{$\m@th
        \mkern \@dotsep mu\hbox{.}\mkern \@dotsep
        mu$}\hfill
     \nobreak
     \hb@xt@\@pnumwidth{\hfil\normalfont \normalcolor #5}%
     \par}%
  \fi}
%
\def\switcht@albion{%
\def\abstractname{Abstract.}
\def\ackname{ACKNOWLEDGMENTS}  %changed by MKL
\def\andname{and}
\def\lastandname{\unskip, and}
\def\appendixname{Appendix}
\def\chaptername{Chapter}
\def\claimname{Claim}
\def\conjecturename{Conjecture}
\def\contentsname{Table of Contents}
\def\corollaryname{Corollary}
\def\definitionname{Definition}
\def\examplename{Example}
\def\exercisename{Exercise}
\def\figurename{Fig.}
\def\keywordname{KEY WORDS:}
\def\indexname{Index}
\def\lemmaname{Lemma}
\def\contriblistname{List of Contributors}
\def\listfigurename{List of Figures}
\def\listtablename{List of Tables}
\def\mailname{{\it Correspondence to\/}:}
\def\noteaddname{Note added in proof}
\def\notename{Note}
\def\partname{Part}
\def\problemname{Problem}
\def\proofname{Proof}
\def\propertyname{Property}
\def\propositionname{Proposition}
\def\questionname{Question}
\def\remarkname{Remark}
\def\refname{REFERENCES}  %MKL
\def\seename{see}
\def\solutionname{Solution}
\def\subclassname{{\it Subject Classifications\/}:}
\def\tablename{Table}
\def\theoremname{Theorem}
\def\titlepageheader{International Journal of Parallel Programming,
                     Vol. X, No. X, XXX 200X
                     (\copyright 200X)}} %MKL
\switcht@albion
% Names of theorem like environments are already defined
% but must be translated if another language is chosen
%
% French section
\def\switcht@francais{%\typeout{On parle francais.}%
 \def\abstractname{R\'esum\'e.}%
 \def\ackname{Remerciements.}%
 \def\andname{et}%
 \def\lastandname{ et}%
 \def\appendixname{Appendice}
 \def\chaptername{Chapitre}%
 \def\claimname{Pr\'etention}%
 \def\conjecturename{Hypoth\`ese}%
 \def\contentsname{Table des mati\`eres}%
 \def\corollaryname{Corollaire}%
 \def\definitionname{D\'efinition}%
 \def\examplename{Exemple}%
 \def\exercisename{Exercice}%
 \def\figurename{Fig.}%
 \def\keywordname{{\bf Mots-cl\'e:}}
 \def\indexname{Index}
 \def\lemmaname{Lemme}%
 \def\contriblistname{Liste des contributeurs}
 \def\listfigurename{Liste des figures}%
 \def\listtablename{Liste des tables}%
 \def\mailname{{\it Correspondence to\/}:}
 \def\noteaddname{Note ajout\'ee \`a l'\'epreuve}%
 \def\notename{Remarque}%
 \def\partname{Partie}%
 \def\problemname{Probl\`eme}%
 \def\proofname{Preuve}%
 \def\propertyname{Caract\'eristique}%
%\def\propositionname{Proposition}%
 \def\questionname{Question}%
 \def\remarkname{Remarque}%
 \def\seename{voir}
 \def\solutionname{Solution}%
 \def\subclassname{{\it Subject Classifications\/}:}
 \def\tablename{Tableau}%
 \def\theoremname{Th\'eor\`eme}%
}
%
% German section
\def\switcht@deutsch{%\typeout{Man spricht deutsch.}%
 \def\abstractname{Zusammenfassung.}%
 \def\ackname{Danksagung.}%
 \def\andname{und}%
 \def\lastandname{ und}%
 \def\appendixname{Anhang}%
 \def\chaptername{Kapitel}%
 \def\claimname{Behauptung}%
 \def\conjecturename{Hypothese}%
 \def\contentsname{Inhaltsverzeichnis}%
 \def\corollaryname{Korollar}%
%\def\definitionname{Definition}%
 \def\examplename{Beispiel}%
 \def\exercisename{\"Ubung}%
 \def\figurename{Abb.}%
 \def\keywordname{{\bf Schl\"usselw\"orter:}}
 \def\indexname{Index}
%\def\lemmaname{Lemma}%
 \def\contriblistname{Mitarbeiter}
 \def\listfigurename{Abbildungsverzeichnis}%
 \def\listtablename{Tabellenverzeichnis}%
 \def\mailname{{\it Correspondence to\/}:}
 \def\noteaddname{Nachtrag}%
 \def\notename{Anmerkung}%
 \def\partname{Teil}%
%\def\problemname{Problem}%
 \def\proofname{Beweis}%
 \def\propertyname{Eigenschaft}%
%\def\propositionname{Proposition}%
 \def\questionname{Frage}%
 \def\remarkname{Anmerkung}%
 \def\seename{siehe}
 \def\solutionname{L\"osung}%
 \def\subclassname{{\it Subject Classifications\/}:}
 \def\tablename{Tabelle}%
%\def\theoremname{Theorem}%
}

% Ragged bottom for the actual page
\def\thisbottomragged{\def\@textbottom{\vskip\z@ plus.0001fil
\global\let\@textbottom\relax}}

%\renewcommand\small{%
%   \@setfontsize\small\@ixpt{12}%  11
%   \abovedisplayskip 8.5\p@ \@plus3\p@ \@minus4\p@
%   \abovedisplayshortskip \z@ \@plus2\p@
%   \belowdisplayshortskip 4\p@ \@plus2\p@ \@minus2\p@
%   \def\@listi{\leftmargin\leftmargini
%               \parsep 0\p@ \@plus1\p@ \@minus\p@
%               \topsep 8\p@ \@plus2\p@ \@minus4\p@
%               \itemsep0\p@}%
%   \belowdisplayskip \abovedisplayskip
%}

\frenchspacing
\widowpenalty=10000
\clubpenalty=10000

% note that LaTeX measures all distances from a point one inch from the top of
% the paper and one inch from the left edge
\if@draftmode 
  \setlength\oddsidemargin   {0\p@}  % see comments in textwidth
  \setlength\evensidemargin  {0\p@}  % "
\else
  \setlength\oddsidemargin   {71\p@}  %63
  \setlength\evensidemargin  {71\p@}  %63
\fi
\setlength\marginparwidth  {90\p@}

\par\addvspace{\topsep}  

\setlength\headsep   {16\p@}

\setlength\footnotesep{7.7\p@}
%\setlength\textfloatsep{8mm\@plus 2\p@ \@minus 4\p@}
\setlength\textfloatsep{4mm\@plus 2\p@ \@minus 4\p@}
%\setlength\intextsep   {8mm\@plus 2\p@ \@minus 2\p@}
\setlength\intextsep   {4mm\@plus 2\p@ \@minus 2\p@}

\setcounter{secnumdepth}{3}  % LNCS:2 
                             % allow section, subsection and subsubsection  
\newcounter {chapter}
\renewcommand\thechapter      {\@arabic\c@chapter}

\newif\if@mainmatter \@mainmattertrue
\newcommand\frontmatter{\cleardoublepage
            \@mainmatterfalse\pagenumbering{Roman}}
\newcommand\mainmatter{\cleardoublepage
       \@mainmattertrue\pagenumbering{arabic}}
\newcommand\backmatter{\if@openright\cleardoublepage\else\clearpage\fi
      \@mainmatterfalse}

\renewcommand\part{\cleardoublepage
                 \thispagestyle{empty}%
                 \if@twocolumn
                     \onecolumn
                     \@tempswatrue
                   \else
                     \@tempswafalse
                 \fi
                 \null\vfil
                 \secdef\@part\@spart}

\def\@part[#1]#2{%
    \ifnum \c@secnumdepth >-2\relax
      \refstepcounter{part}%
      \addcontentsline{toc}{part}{\thepart\hspace{1em}#1}%
    \else
      \addcontentsline{toc}{part}{#1}%
    \fi
    \markboth{}{}%
    {\centering
     \interlinepenalty \@M
     \normalfont
     \ifnum \c@secnumdepth >-2\relax
       \huge\bfseries \partname~\thepart
       \par
       \vskip 20\p@
     \fi
     \Huge \bfseries #2\par}%
    \@endpart}
\def\@spart#1{%
    {\centering
     \interlinepenalty \@M
     \normalfont
     \Huge \bfseries #1\par}%
    \@endpart}
\def\@endpart{\vfil\newpage
              \if@twoside
                \null
                \thispagestyle{empty}%
                \newpage
              \fi
              \if@tempswa
                \twocolumn
              \fi}

\newcommand\chapter{\clearpage
                    \thispagestyle{empty}%
                    \global\@topnum\z@
                    \@afterindentfalse
                    \secdef\@chapter\@schapter}
\def\@chapter[#1]#2{\ifnum \c@secnumdepth >\m@ne
                       \if@mainmatter
                         \refstepcounter{chapter}%
                         \typeout{\@chapapp\space\thechapter.}%
                         \addcontentsline{toc}{chapter}%
                                  {\protect\numberline{\thechapter}#1}%
                       \else
                         \addcontentsline{toc}{chapter}{#1}%
                       \fi
                    \else
                      \addcontentsline{toc}{chapter}{#1}%
                    \fi
                    \chaptermark{#1}%
                    \addtocontents{lof}{\protect\addvspace{10\p@}}%
                    \addtocontents{lot}{\protect\addvspace{10\p@}}%
                    \if@twocolumn
                      \@topnewpage[\@makechapterhead{#2}]%
                    \else
                      \@makechapterhead{#2}%
                      \@afterheading
                    \fi}
\def\@makechapterhead#1{%
% \vspace*{50\p@}%
  {\centering
    \ifnum \c@secnumdepth >\m@ne
      \if@mainmatter
        \large\bfseries \@chapapp{} \thechapter
        \par\nobreak
        \vskip 20\p@
      \fi
    \fi
    \interlinepenalty\@M
    \Large \bfseries #1\par\nobreak
    \vskip 40\p@
  }}
\def\@schapter#1{\if@twocolumn
                   \@topnewpage[\@makeschapterhead{#1}]%
                 \else
                   \@makeschapterhead{#1}%
                   \@afterheading
                 \fi}
\def\@makeschapterhead#1{%
% \vspace*{50\p@}%
  {\centering
    \normalfont
    \interlinepenalty\@M
    \Large \bfseries  #1\par\nobreak
    \vskip 40\p@
  }}

\renewcommand\section{\@startsection{section}{1}{\z@}%   
%                       {-18\p@ \@plus -4\p@ \@minus -4\p@}%
                       {-14\p@ \@plus -4\p@ \@minus -4\p@}%
%                       {12\p@ \@plus 4\p@ \@minus 4\p@}%  
                       {10\p@ \@plus 4\p@ \@minus 4\p@}%  
                       {\if@draftmode  % changed by Ming Kin Lai
                           \normalfont
                        \else
                           \sffamily\small
                        \fi               
%                        \large\bfseries\boldmath
                        \bfseries%\boldmath  
                        \rightskip=\z@ \@plus 8em\pretolerance=10000 \MakeUppercase}}
\renewcommand\subsection{\@startsection{subsection}{2}{\z@}%
%                       {-18\p@ \@plus -4\p@ \@minus -4\p@}%
                       {-14\p@ \@plus -4\p@ \@minus -4\p@}%
%                       {8\p@ \@plus 4\p@ \@minus 4\p@}%
                       {6\p@ \@plus 4\p@ \@minus 4\p@}%
%                       {\normalfont\normalsize\bfseries\boldmath
                       {\if@draftmode  % changed by Ming Kin Lai
                           \normalfont
                        \else
                           \sffamily\small
                        \fi
%                        \normalsize\bfseries\boldmath
                        \bfseries%\boldmath
                        \rightskip=\z@ \@plus 8em\pretolerance=10000 }}
\renewcommand\subsubsection{\@startsection{subsubsection}{3}{\z@}%
%                       {-18\p@ \@plus -4\p@ \@minus -4\p@}%
                       {-14\p@ \@plus -4\p@ \@minus -4\p@}%
%                       {8\p@ \@plus 4\p@ \@minus 4\p@}% % changed by MKL
                       {6\p@ \@plus 4\p@ \@minus 4\p@}% % changed by MKL
%                       {-0.5em \@plus -0.22em \@minus -0.1em}%
%                       {\normalfont\normalsize\slshape\boldmath}}
                       {\if@draftmode  % changed by Ming Kin Lai 
                           \normalfont 
                        \else 
                           \sffamily\slshape 
                        \fi 
                        \normalsize
                        \rightskip=\z@ \@plus 8em\pretolerance=10000 }} %added by MKL
\renewcommand\paragraph{\@startsection{paragraph}{4}{\z@}%
                       {-12\p@ \@plus -4\p@ \@minus -4\p@}%
                       {-0.5em \@plus -0.22em \@minus -0.1em}%
                       {\normalfont\normalsize\itshape}}
\renewcommand\subparagraph[1]{\typeout{LLNCS warning: You should not use
                  \string\subparagraph\space with this class}\vskip0.5cm
You should not use \verb|\subparagraph| with this class.\vskip0.5cm}

\renewcommand{\@seccntformat}[1]{\@nameuse{the#1}.\quad}  %added by MKL to add a period after the section name

% commented out by M K Lai - begin
%\DeclareMathSymbol{\Gamma}{\mathalpha}{letters}{"00}
%\DeclareMathSymbol{\Delta}{\mathalpha}{letters}{"01}
%\DeclareMathSymbol{\Theta}{\mathalpha}{letters}{"02}
%\DeclareMathSymbol{\Lambda}{\mathalpha}{letters}{"03}
%\DeclareMathSymbol{\Xi}{\mathalpha}{letters}{"04}
%\DeclareMathSymbol{\Pi}{\mathalpha}{letters}{"05}
%\DeclareMathSymbol{\Sigma}{\mathalpha}{letters}{"06}
%\DeclareMathSymbol{\Upsilon}{\mathalpha}{letters}{"07}
%\DeclareMathSymbol{\Phi}{\mathalpha}{letters}{"08}
%\DeclareMathSymbol{\Psi}{\mathalpha}{letters}{"09}
%\DeclareMathSymbol{\Omega}{\mathalpha}{letters}{"0A}
% commented out by M K Lai - end

\let\footnotesizesv\footnotesize  % save the footnotesize - Ming Kin Lai
\let\footnotesize\small
\newcommand{\smallersize}{\fontsize{8.5}{10.0}\selectfont}

\if@custvec
\def\vec#1{\mathchoice{\mbox{\boldmath$\displaystyle#1$}}
{\mbox{\boldmath$\textstyle#1$}}
{\mbox{\boldmath$\scriptstyle#1$}}
{\mbox{\boldmath$\scriptscriptstyle#1$}}}
\fi

\def\squareforqed{\hbox{\rlap{$\sqcap$}$\sqcup$}}
\def\qed{\ifmmode\squareforqed\else{\unskip\nobreak\hfil
\penalty50\hskip1em\null\nobreak\hfil\squareforqed
\parfillskip=0pt\finalhyphendemerits=0\endgraf}\fi}

\def\getsto{\mathrel{\mathchoice {\vcenter{\offinterlineskip
\halign{\hfil
$\displaystyle##$\hfil\cr\gets\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr\gets
\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr\gets
\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
\gets\cr\to\cr}}}}}
\def\lid{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr<\cr
\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr<\cr
\noalign{\vskip1pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr
\noalign{\vskip0.9pt}=\cr}}}}}
\def\gid{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr>\cr
\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr>\cr
\noalign{\vskip1pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr
\noalign{\vskip0.9pt}=\cr}}}}}
\def\grole{\mathrel{\mathchoice {\vcenter{\offinterlineskip
\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\noalign{\vskip-1pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
>\cr\noalign{\vskip-1pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
>\cr\noalign{\vskip-0.8pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr\noalign{\vskip-0.3pt}<\cr}}}}}
\def\bbbr{{\rm I\!R}} %reelle Zahlen
\def\bbbm{{\rm I\!M}}
\def\bbbn{{\rm I\!N}} %natuerliche Zahlen
\def\bbbf{{\rm I\!F}}
\def\bbbh{{\rm I\!H}}
\def\bbbk{{\rm I\!K}}
\def\bbbp{{\rm I\!P}}
\def\bbbone{{\mathchoice {\rm 1\mskip-4mu l} {\rm 1\mskip-4mu l}
{\rm 1\mskip-4.5mu l} {\rm 1\mskip-5mu l}}}
\def\bbbc{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}}}
\def\bbbq{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm
Q$}\hbox{\raise
0.15\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.8\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm Q$}\hbox{\raise
0.15\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.8\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm Q$}\hbox{\raise
0.15\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.7\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm Q$}\hbox{\raise
0.15\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.7\ht0\hss}\box0}}}}
\def\bbbt{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm
T$}\hbox{\hbox to0pt{\kern0.3\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm T$}\hbox{\hbox
to0pt{\kern0.3\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm T$}\hbox{\hbox
to0pt{\kern0.3\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm T$}\hbox{\hbox
to0pt{\kern0.3\wd0\vrule height0.9\ht0\hss}\box0}}}}
\def\bbbs{{\mathchoice
{\setbox0=\hbox{$\displaystyle     \rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.35\wd0\vrule height0.45\ht0\hss}\hbox
to0pt{\kern0.55\wd0\vrule height0.5\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle        \rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.35\wd0\vrule height0.45\ht0\hss}\hbox
to0pt{\kern0.55\wd0\vrule height0.5\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle      \rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.35\wd0\vrule height0.45\ht0\hss}\raise0.05\ht0\hbox
to0pt{\kern0.5\wd0\vrule height0.45\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.4\wd0\vrule height0.45\ht0\hss}\raise0.05\ht0\hbox
to0pt{\kern0.55\wd0\vrule height0.45\ht0\hss}\box0}}}}
\def\bbbz{{\mathchoice {\hbox{$\mathsf\textstyle Z\kern-0.4em Z$}}
{\hbox{$\mathsf\textstyle Z\kern-0.4em Z$}}
{\hbox{$\mathsf\scriptstyle Z\kern-0.3em Z$}}
{\hbox{$\mathsf\scriptscriptstyle Z\kern-0.2em Z$}}}}

\let\ts\,

\setlength\leftmargini  {17\p@}
\setlength\leftmargin    {\leftmargini}
\setlength\leftmarginii  {\leftmargini}
\setlength\leftmarginiii {\leftmargini}
\setlength\leftmarginiv  {\leftmargini}
\setlength  \labelsep  {.5em}
\setlength  \labelwidth{\leftmargini}
\addtolength\labelwidth{-\labelsep}

\def\@listI{\leftmargin\leftmargini
            \parsep 3\p@ \@plus1\p@ \@minus\p@
            \topsep 8\p@ \@plus2\p@ \@minus4\p@
            \itemsep3\p@}   %RRR
\let\@listi\@listI
\@listi
\def\@listii {\leftmargin\leftmarginii
              \labelwidth\leftmarginii
              \advance\labelwidth-\labelsep
              \topsep    0\p@ \@plus2\p@ \@minus\p@}
\def\@listiii{\leftmargin\leftmarginiii
              \labelwidth\leftmarginiii
              \advance\labelwidth-\labelsep
              \topsep    0\p@ \@plus\p@\@minus\p@
              \parsep    \z@
              \partopsep \p@ \@plus\z@ \@minus\p@}

\renewcommand\labelitemi{\normalfont\bfseries --}
\renewcommand\labelitemii{$\m@th\bullet$}

\setlength\arraycolsep{1.4\p@}
\setlength\tabcolsep{1.4\p@}

\def\tableofcontents{\chapter*{\contentsname\@mkboth{{\contentsname}}%
                                                    {{\contentsname}}}
 \def\authcount##1{\setcounter{auco}{##1}\setcounter{@auth}{1}}
 \def\lastand{\ifnum\value{auco}=2\relax
                 \unskip{} \andname\
              \else
                 \unskip \lastandname\
              \fi}%
 \def\and{\stepcounter{@auth}\relax
          \ifnum\value{@auth}=\value{auco}%
             \lastand
          \else
             \unskip,
          \fi}%
 \@starttoc{toc}\if@restonecol\twocolumn\fi}

\def\l@part#1#2{\addpenalty{\@secpenalty}%
   \addvspace{2em plus\p@}%  % space above part line
   \begingroup
     \parindent \z@
     \rightskip \z@ plus 5em
     \hrule\vskip5pt
     \large               % same size as for a contribution heading
     \bfseries\boldmath   % set line in boldface
     \leavevmode          % TeX command to enter horizontal mode.
     #1\par
     \vskip5pt
     \hrule
     \vskip1pt
     \nobreak             % Never break after part entry
   \endgroup}

\def\@dotsep{2}

\def\hyperhrefextend{\ifx\hyper@anchor\@undefined\else
{chapter.\thechapter}\fi}

\def\addnumcontentsmark#1#2#3{%
\addtocontents{#1}{\protect\contentsline{#2}{\protect\numberline
                     {\thechapter}#3}{\thepage}\hyperhrefextend}}
\def\addcontentsmark#1#2#3{%
\addtocontents{#1}{\protect\contentsline{#2}{#3}{\thepage}\hyperhrefextend}}
\def\addcontentsmarkwop#1#2#3{%
\addtocontents{#1}{\protect\contentsline{#2}{#3}{0}\hyperhrefextend}}

\def\@adcmk[#1]{\ifcase #1 \or
\def\@gtempa{\addnumcontentsmark}%
  \or    \def\@gtempa{\addcontentsmark}%
  \or    \def\@gtempa{\addcontentsmarkwop}%
  \fi\@gtempa{toc}{chapter}}
\def\addtocmark{\@ifnextchar[{\@adcmk}{\@adcmk[3]}}

\def\l@chapter#1#2{\addpenalty{-\@highpenalty}
 \vskip 1.0em plus 1pt \@tempdima 1.5em \begingroup
 \parindent \z@ \rightskip \@tocrmarg
 \advance\rightskip by 0pt plus 2cm
 \parfillskip -\rightskip \pretolerance=10000
 \leavevmode \advance\leftskip\@tempdima \hskip -\leftskip
 {\large\bfseries\boldmath#1}\ifx0#2\hfil\null
 \else
      \nobreak
      \leaders\hbox{$\m@th \mkern \@dotsep mu.\mkern
      \@dotsep mu$}\hfill
      \nobreak\hbox to\@pnumwidth{\hss #2}%
 \fi\par
 \penalty\@highpenalty \endgroup}

\def\l@title#1#2{\addpenalty{-\@highpenalty}
 \addvspace{8pt plus 1pt}
 \@tempdima \z@
 \begingroup
 \parindent \z@ \rightskip \@tocrmarg
 \advance\rightskip by 0pt plus 2cm
 \parfillskip -\rightskip \pretolerance=10000
 \leavevmode \advance\leftskip\@tempdima \hskip -\leftskip
 #1\nobreak
 \leaders\hbox{$\m@th \mkern \@dotsep mu.\mkern
 \@dotsep mu$}\hfill
 \nobreak\hbox to\@pnumwidth{\hss #2}\par
 \penalty\@highpenalty \endgroup}

\def\l@author#1#2{\addpenalty{\@highpenalty}
 \@tempdima=\z@ %15\p@
 \begingroup
 \parindent \z@ \rightskip \@tocrmarg
 \advance\rightskip by 0pt plus 2cm
 \pretolerance=10000
 \leavevmode \advance\leftskip\@tempdima %\hskip -\leftskip
 \textit{#1}\par
 \penalty\@highpenalty \endgroup}

\setcounter{tocdepth}{0}
\newdimen\tocchpnum
\newdimen\tocsecnum
\newdimen\tocsectotal
\newdimen\tocsubsecnum
\newdimen\tocsubsectotal
\newdimen\tocsubsubsecnum
\newdimen\tocsubsubsectotal
\newdimen\tocparanum
\newdimen\tocparatotal
\newdimen\tocsubparanum
\tocchpnum=\z@            % no chapter numbers
\tocsecnum=15\p@          % section 88. plus 2.222pt
\tocsubsecnum=23\p@       % subsection 88.8 plus 2.222pt
\tocsubsubsecnum=27\p@    % subsubsection 88.8.8 plus 1.444pt
\tocparanum=35\p@         % paragraph 88.8.8.8 plus 1.666pt
\tocsubparanum=43\p@      % subparagraph 88.8.8.8.8 plus 1.888pt
\def\calctocindent{%
\tocsectotal=\tocchpnum
\advance\tocsectotal by\tocsecnum
\tocsubsectotal=\tocsectotal
\advance\tocsubsectotal by\tocsubsecnum
\tocsubsubsectotal=\tocsubsectotal
\advance\tocsubsubsectotal by\tocsubsubsecnum
\tocparatotal=\tocsubsubsectotal
\advance\tocparatotal by\tocparanum}
\calctocindent

\def\l@section{\@dottedtocline{1}{\tocchpnum}{\tocsecnum}}
\def\l@subsection{\@dottedtocline{2}{\tocsectotal}{\tocsubsecnum}}
\def\l@subsubsection{\@dottedtocline{3}{\tocsubsectotal}{\tocsubsubsecnum}}
\def\l@paragraph{\@dottedtocline{4}{\tocsubsubsectotal}{\tocparanum}}
\def\l@subparagraph{\@dottedtocline{5}{\tocparatotal}{\tocsubparanum}}

% commented out by Ming Kin Lai so that endfloat would work - begin
%\def\listoffigures{\@restonecolfalse\if@twocolumn\@restonecoltrue\onecolumn
% 
%\fi\section*{\listfigurename\@mkboth{{\listfigurename}}{{\listfigurename}}}
% \@starttoc{lof}\if@restonecol\twocolumn\fi}
%\def\l@figure{\@dottedtocline{1}{0em}{1.5em}}
%
%\def\listoftables{\@restonecolfalse\if@twocolumn\@restonecoltrue\onecolumn
% 
%\fi\section*{\listtablename\@mkboth{{\listtablename}}{{\listtablename}}}
% \@starttoc{lot}\if@restonecol\twocolumn\fi}
%\let\l@table\l@figure
%
%\renewcommand\listoffigures{%
%    \section*{\listfigurename
%      \@mkboth{\listfigurename}{\listfigurename}}%
%    \@starttoc{lof}%
%    }
%
%\renewcommand\listoftables{%
%    \section*{\listtablename
%      \@mkboth{\listtablename}{\listtablename}}%
%    \@starttoc{lot}%
%    }
% commented out by Ming Kin Lai so that endfloat would work - end

%added by Ming Kin Lai - begin
%IJPP style does not leave a space between the word/punctuation
%right before the citation and the citation itself, i.e.,
%one should write "...performance\cite{Lai:2003}"
%rather than "...performance \cite{Lai:2003}".
%Also, IJPP style is such that the period or comma comes
%before the citation rather than after it.  So, one
%should write "...based on MPI.\cite{MPI}" rather than
%"...based on MPI\cite{MPI}."
%IJPP requires a citation in parenthesis and in superscripts.
\def\@cite#1#2{\textsuperscript{(#1\if@tempswa , #2\fi)}}
%added by Ming Kin Lai - end

\ifx\oribibl\undefined
\ifx\citeauthoryear\undefined
\renewenvironment{thebibliography}[1]
     {\section*{\refname}
      \def\@biblabel##1{##1.}
%      \small %LNCS
      \if@draftmode
      \else
         \smallersize
      \fi
      \list{\@biblabel{\@arabic\c@enumiv}}%
           {\settowidth\labelwidth{\@biblabel{#1}}%
            \leftmargin\labelwidth
            \advance\leftmargin\labelsep
            \if@openbib
              \advance\leftmargin\bibindent
              \itemindent -\bibindent
              \listparindent \itemindent
              \parsep \z@
            \fi
            \usecounter{enumiv}%
            \let\p@enumiv\@empty
            \renewcommand\theenumiv{\@arabic\c@enumiv}}%
      \if@openbib
        \renewcommand\newblock{\par}%
      \else
        \renewcommand\newblock{\hskip .11em \@plus.33em \@minus.07em}%
      \fi
      \sloppy\clubpenalty4000\widowpenalty4000%
      \sfcode`\.=\@m}
     {\def\@noitemerr
       {\@latex@warning{Empty `thebibliography' environment}}%
      \endlist}
%xxx \def\@lbibitem[#1]#2{\item[{[#1]}\hfill]\if@filesw
\def\@lbibitem(#1)#2{\item[{(#1)}\hfill]\if@filesw
     {\let\protect\noexpand\immediate
     \write\@auxout{\string\bibcite{#2}{#1}}}\fi\ignorespaces}
\newcount\@tempcntc
%xxx \def\@citex[#1]#2{\if@filesw\immediate\write\@auxout{\string\citation{#2}}\fi
\def\@citex[#1]#2{\if@filesw\immediate\write\@auxout{\string\citation{#2}}\fi
  \@tempcnta\z@\@tempcntb\m@ne\def\@citea{}\@cite{\@for\@citeb:=#2\do
    {\@ifundefined
       {b@\@citeb}{\@citeo\@tempcntb\m@ne\@citea\def\@citea{,}{\bfseries
        ?}\@warning
       {Citation `\@citeb' on page \thepage \space undefined}}%
    {\setbox\z@\hbox{\global\@tempcntc0\csname b@\@citeb\endcsname\relax}%
     \ifnum\@tempcntc=\z@ \@citeo\@tempcntb\m@ne
       \@citea\def\@citea{,}\hbox{\csname b@\@citeb\endcsname}%
     \else
      \advance\@tempcntb\@ne
      \ifnum\@tempcntb=\@tempcntc
      \else\advance\@tempcntb\m@ne\@citeo
      \@tempcnta\@tempcntc\@tempcntb\@tempcntc\fi\fi}}\@citeo}{#1}}
\def\@citeo{\ifnum\@tempcnta>\@tempcntb\else
               \@citea\def\@citea{,\,\hskip\z@skip}%
               \ifnum\@tempcnta=\@tempcntb\the\@tempcnta\else
               {\advance\@tempcnta\@ne\ifnum\@tempcnta=\@tempcntb \else
                \def\@citea{--}\fi
      \advance\@tempcnta\m@ne\the\@tempcnta\@citea\the\@tempcntb}\fi\fi}
\else
\renewenvironment{thebibliography}[1]
     {\section*{\refname}
      \small
      \list{}%
           {\settowidth\labelwidth{}%
            \leftmargin\parindent
            \itemindent=-\parindent
            \labelsep=\z@
            \if@openbib
              \advance\leftmargin\bibindent
              \itemindent -\bibindent
              \listparindent \itemindent
              \parsep \z@
            \fi
            \usecounter{enumiv}%
            \let\p@enumiv\@empty
            \renewcommand\theenumiv{}}%
      \if@openbib
        \renewcommand\newblock{\par}%
      \else
        \renewcommand\newblock{\hskip .11em \@plus.33em \@minus.07em}%
      \fi
      \sloppy\clubpenalty4000\widowpenalty4000%
      \sfcode`\.=\@m}
     {\def\@noitemerr
       {\@latex@warning{Empty `thebibliography' environment}}%
      \endlist}
      \def\@cite#1{#1}%
%xxx      \def\@lbibitem[#1]#2{\item[]\if@filesw
      \def\@lbibitem(#1)#2{\item()\if@filesw
        {\def\protect##1{\string ##1\space}\immediate
      \write\@auxout{\string\bibcite{#2}{#1}}}\fi\ignorespaces}
   \fi
\else
\@cons\@openbib@code{\noexpand\small}
\fi

\def\idxquad{\hskip 10\p@}% space that divides entry from number

\def\@idxitem{\par\hangindent 10\p@}

\def\subitem{\par\setbox0=\hbox{--\enspace}% second order
                \noindent\hangindent\wd0\box0}% index entry

\def\subsubitem{\par\setbox0=\hbox{--\,--\enspace}% third
                \noindent\hangindent\wd0\box0}% order index entry

\def\indexspace{\par \vskip 10\p@ plus5\p@ minus3\p@\relax}

\renewenvironment{theindex}
               {\@mkboth{\indexname}{\indexname}%
                \thispagestyle{empty}\parindent\z@
                \parskip\z@ \@plus .3\p@\relax
                \let\item\par
                \def\,{\relax\ifmmode\mskip\thinmuskip
                             \else\hskip0.2em\ignorespaces\fi}%
                \normalfont\small
                \begin{multicols}{2}[\@makeschapterhead{\indexname}]%
                }
                {\end{multicols}}

\renewcommand\footnoterule{%
  \kern-3\p@
  \if@draftmode
    \hrule\@width 2truecm
  \else
  \fi
  \kern2.6\p@}
  \newdimen\fnindent
  \fnindent1em  
  \long\def\@makefntext#1{%
    \parindent \fnindent%
    \leftskip \fnindent%
    \noindent
    \llap{\hb@xt@1em{\hss\@makefnmark\ }}\ignorespaces#1}  

\long\def\@makecaption#1#2{%
  \vskip\abovecaptionskip
%  \sbox\@tempboxa{{\bfseries #1.} #2}% % changed by MKL
  \sbox\@tempboxa{#1. #2}%
  \ifdim \wd\@tempboxa >\hsize
%    {\bfseries #1.} #2\par % changed by MKL
     #1. #2\par
  \else
    \global \@minipagefalse
    \hb@xt@\hsize{\hfil\box\@tempboxa\hfil}%
  \fi
  \vskip\belowcaptionskip}

\def\fps@figure{htbp}
\def\fnum@figure{\figurename\thinspace\thefigure} %MMM
\def \@floatboxreset {%
        \reset@font
        \small
        \@setnobreak
        \@setminipage
}
\def\fps@table{htbp}
\def\thetable{\@Roman\c@table}  %added by MKLai
\def\fnum@table{\tablename~\thetable}
\renewenvironment{table}
               {\setlength\abovecaptionskip{0\p@}%
%                \setlength\belowcaptionskip{10\p@}%  LNCS
                \setlength\belowcaptionskip{5\p@}%  changed by MKL
                \@float{table}}
               {\end@float}
\renewenvironment{table*}
               {\setlength\abovecaptionskip{0\p@}%
%                \setlength\belowcaptionskip{10\p@}%
                \setlength\belowcaptionskip{5\p@}%
                \@dblfloat{table}}
               {\end@dblfloat}

%In case of draftmode,
%Preferably, if #1.EQ.Table, make the entire caption boldface and Helvet - MKL
%also, make the entire caption smaller in font size - MKL
\long\def\@caption#1[#2]#3{\par\addcontentsline{\csname
  ext@#1\endcsname}{#1}{\protect\numberline{\csname
  the#1\endcsname}{\ignorespaces #2}}\begingroup
    \@parboxrestore
    \@makecaption{\csname fnum@#1\endcsname}{\ignorespaces #3}\par
  \endgroup}

% LaTeX does not provide a command to enter the authors institute
% addresses. The \institute command is defined here.

\newcounter{@inst}
\newcounter{@auth}
\newcounter{auco}
\newdimen\instindent
\newbox\authrun
\newtoks\authorrunning
\newtoks\tocauthor
\newbox\titrun
\newtoks\titlerunning
\newtoks\toctitle

\def\clearheadinfo{\gdef\@author{No Author Given}%
                   \gdef\@title{No Title Given}%
                   \gdef\@subtitle{}%
                   \gdef\@institute{No Institute Given}%
                   \gdef\@thanks{}%
                   \global\titlerunning={}\global\authorrunning={}%
                   \global\toctitle={}\global\tocauthor={}}

\def\institute#1{\gdef\@institute{#1}}

\def\institutename{\par
 \begingroup
% added by Ming Kin Lai - begin
 \if@draftmode
 \else
    \footnotesizesv\smallersize
 \fi  
% added by Ming Kin Lai - end
\if@draftmode
  \parskip=4\p@
\else
  \parskip=\z@
\fi
 \parindent=\z@
 \setcounter{@inst}{1}%
 \def\and{\par\stepcounter{@inst}%
 \noindent$^{\the@inst}$\enspace\ignorespaces}%
 \setbox0=\vbox{\def\thanks##1{}\@institute}%
% \ifnum\c@@inst=1\relax  % institute numbering and footnote numbering bug in llncs.cls fixed by MKL
%   \gdef\fnnstart{0}%    
% \else
   \xdef\fnnstart{\c@@inst}%
   \setcounter{@inst}{1}%
   \noindent$^{\the@inst}$\enspace
% \fi
 \ignorespaces
 \@institute\par
 \endgroup}

\def\@fnsymbol#1{\ensuremath{\ifcase#1\or\star\or{\star\star}\or
   {\star\star\star}\or \dagger\or \ddagger\or
   \mathchar "278\or \mathchar "27B\or \|\or **\or \dagger\dagger
   \or \ddagger\ddagger \else\@ctrerr\fi}}

\def\inst#1{\unskip$^{#1}$}
\def\fnmsep{\unskip$^,$}
\def\email#1{{\tt#1}}
\AtBeginDocument{\@ifundefined{url}{\def\url#1{#1}}{}%
\@ifpackageloaded{babel}{%
\@ifundefined{extrasenglish}{}{\addto\extrasenglish{\switcht@albion}}%
\@ifundefined{extrasfrenchb}{}{\addto\extrasfrenchb{\switcht@francais}}%
\@ifundefined{extrasgerman}{}{\addto\extrasgerman{\switcht@deutsch}}%
}{\switcht@@therlang}%
}
\def\homedir{\~{ }}

\def\subtitle#1{\gdef\@subtitle{#1}}
\clearheadinfo

\renewcommand\maketitle{\newpage

  \linespread{1} % added by MKL
  \refstepcounter{chapter}%
  \stepcounter{section}%
  \setcounter{section}{0}%
  \setcounter{subsection}{0}%
  \setcounter{figure}{0}
  \setcounter{table}{0}
  \setcounter{equation}{0}
  \setcounter{footnote}{0}%
  \begingroup
    \parindent=\z@
    \renewcommand\thefootnote{\@fnsymbol\c@footnote}%
    \if@twocolumn
      \ifnum \col@number=\@ne
        \@maketitle
      \else
        \twocolumn[\@maketitle]%
      \fi
    \else
      \newpage
      \global\@topnum\z@   % Prevents figures from going at top of page.
      \@maketitle
    \fi
%    \thispagestyle{plain}\@thanks  %changed by Ming Kin Lai
    \thispagestyle{titlepage}\@thanks
%
    \def\\{\unskip\ \ignorespaces}\def\inst##1{\unskip{}}%
    \def\thanks##1{\unskip{}}\def\fnmsep{\unskip}%
    \instindent=\hsize
    \advance\instindent by-\headlineindent
    \if!\the\toctitle!\addcontentsline{toc}{title}{\@title}\else
       \addcontentsline{toc}{title}{\the\toctitle}\fi
    \if@runhead
       \if!\the\titlerunning!\else
         \edef\@title{\the\titlerunning}%
       \fi
% finalmode formatting for running head - title added by Ming Kin Lai - begin     
       \if@draftmode%    
%         \global\setbox\titrun=\hbox{\small\rm\unboldmath\ignorespaces\@title}%
       \else% 
         \global\setbox\titrun=\hbox{\footnotesizesv\rm\unboldmath\ignorespaces\bfseries\textsf{\@title}}%
       \fi
% finalmode formatting added by Ming Kin Lai - end     
       \ifdim\wd\titrun>\instindent
          \typeout{Title too long for running head. Please supply}%
          \typeout{a shorter form with \string\titlerunning\space prior to
                   \string\maketitle}%
          \global\setbox\titrun=\hbox{\small\rm
          Title Suppressed Due to Excessive Length}%
       \fi
       \xdef\@title{\copy\titrun}%
    \fi
%
    \if!\the\tocauthor!\relax
      {\def\and{\noexpand\protect\noexpand\and}%
      \protected@xdef\toc@uthor{\@author}}%
    \else
      \def\\{\noexpand\protect\noexpand\newline}%
      \protected@xdef\scratch{\the\tocauthor}%
      \protected@xdef\toc@uthor{\scratch}%
    \fi
    \addcontentsline{toc}{author}{\toc@uthor}%
    \if@runhead
       \if!\the\authorrunning!
         \value{@inst}=\value{@auth}%
         \setcounter{@auth}{1}%
       \else
         \edef\@author{\the\authorrunning}%
       \fi
% finalmode formatting for running head - authors added by Ming Kin Lai - begin     
       \if@draftmode%    
%         \global\setbox\authrun=\hbox{\small\unboldmath\@author\unskip}%
       \else%
         \global\setbox\authrun=\hbox{\footnotesizesv\unboldmath\bfseries\textsf{\@author}\unskip}%
       \fi
% finalmode formatting added by Ming Kin Lai - end     
       \ifdim\wd\authrun>\instindent
          \typeout{Names of authors too long for running head. Please supply}%
          \typeout{a shorter form with \string\authorrunning\space prior to
                   \string\maketitle}%
          \global\setbox\authrun=\hbox{\small\rm
          Authors Suppressed Due to Excessive Length}%
       \fi
       \xdef\@author{\copy\authrun}%
       \markboth{\@author}{\@title}%
     \fi
  \endgroup
  \setcounter{footnote}{\fnnstart}%
  \clearheadinfo}
%
\def\@maketitle{\newpage  
\if@draftmode\else%
  \vspace*{0.685in}  % more space added above the title by Ming Kin Lai
\fi
 \markboth{}{}%
 \def\lastand{\ifnum\value{@inst}=2\relax
                 \unskip{} \andname\
              \else
                 \unskip \lastandname\
              \fi}%
 \def\and{\stepcounter{@auth}\relax
          \ifnum\value{@auth}=\value{@inst}%
             \lastand
          \else
             \unskip,
          \fi}%
% \begin{center}%  %changed by MKL from center to flushleft
 \begin{flushleft}%
 \let\newline\\
% {\Large \bfseries\boldmath
  {%
  \pretolerance=10000
% finalmode formatting for title added by Ming Kin Lai - begin
  \if@draftmode%
    \large\textbf{\@title}
  \else%
    \LARGE\textsf{\@title}
  \fi 
  \par}\vskip .4cm
% finalmode formatting added by Ming Kin Lai - end
%  \@title \par}\vskip .8cm
%\if!\@subtitle!\else {\large \bfseries\boldmath
\if!\@subtitle!\else {\large 
  \vskip -.65cm
  \pretolerance=10000
% finalmode formatting for subtitle added by Ming Kin Lai - begin
  \if@draftmode%
    \@subtitle
  \else%
    \huge\textsf{\@subtitle}
  \fi
  \par}\vskip .4cm
% finalmode formatting added by Ming Kin Lai - end
%  \@subtitle \par}\vskip .8cm
\fi
 \setbox0=\vbox{\setcounter{@auth}{1}\def\and{\stepcounter{@auth}}%
 \def\thanks##1{}\@author}%
 \global\value{@inst}=\value{@auth}%
 \global\value{auco}=\value{@auth}%
 \setcounter{@auth}{1}%
{\lineskip .5em
\noindent\ignorespaces
% finalmode formatting for authors added by Ming Kin Lai - begin
\if@draftmode%
  \@author 
\else
  \large\textsf{\@author}
\fi
% finalmode formatting added by Ming Kin Lai - end
\vskip.35cm}
%\@author\vskip.35cm}
\if@draftmode%
 {\small\institutename}
\else
 \institutename
\fi
 \end{flushleft}% 
 }

% definition of the "\spnewtheorem" command.
%
% Usage:
%
%     \spnewtheorem{env_nam}{caption}[within]{cap_font}{body_font}
% or  \spnewtheorem{env_nam}[numbered_like]{caption}{cap_font}{body_font}
% or  \spnewtheorem*{env_nam}{caption}{cap_font}{body_font}
%
% New is "cap_font" and "body_font". It stands for
% fontdefinition of the caption and the text itself.
%
% "\spnewtheorem*" gives a theorem without number.
%
% A defined spnewthoerem environment is used as described
% by Lamport.
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\def\@thmcountersep{}
\def\@thmcounterend{.}

\def\spnewtheorem{\@ifstar{\@sthm}{\@Sthm}}

% definition of \spnewtheorem with number

\def\@spnthm#1#2{%
  \@ifnextchar[{\@spxnthm{#1}{#2}}{\@spynthm{#1}{#2}}}
\def\@Sthm#1{\@ifnextchar[{\@spothm{#1}}{\@spnthm{#1}}}

\def\@spxnthm#1#2[#3]#4#5{\expandafter\@ifdefinable\csname #1\endcsname
   {\@definecounter{#1}\@addtoreset{#1}{#3}%
   \expandafter\xdef\csname the#1\endcsname{\expandafter\noexpand
     \csname the#3\endcsname \noexpand\@thmcountersep \@thmcounter{#1}}%
   \expandafter\xdef\csname #1name\endcsname{#2}%
   \global\@namedef{#1}{\@spthm{#1}{\csname #1name\endcsname}{#4}{#5}}%
                              \global\@namedef{end#1}{\@endtheorem}}}

\def\@spynthm#1#2#3#4{\expandafter\@ifdefinable\csname #1\endcsname
   {\@definecounter{#1}%
   \expandafter\xdef\csname the#1\endcsname{\@thmcounter{#1}}%
   \expandafter\xdef\csname #1name\endcsname{#2}%
   \global\@namedef{#1}{\@spthm{#1}{\csname #1name\endcsname}{#3}{#4}}%
                               \global\@namedef{end#1}{\@endtheorem}}}

\def\@spothm#1[#2]#3#4#5{%
  \@ifundefined{c@#2}{\@latexerr{No theorem environment `#2' defined}\@eha}%
  {\expandafter\@ifdefinable\csname #1\endcsname
  {\global\@namedef{the#1}{\@nameuse{the#2}}%
  \expandafter\xdef\csname #1name\endcsname{#3}%
  \global\@namedef{#1}{\@spthm{#2}{\csname #1name\endcsname}{#4}{#5}}%
  \global\@namedef{end#1}{\@endtheorem}}}}

\def\@spthm#1#2#3#4{\topsep 7\p@ \@plus2\p@ \@minus4\p@
\refstepcounter{#1}%
\@ifnextchar[{\@spythm{#1}{#2}{#3}{#4}}{\@spxthm{#1}{#2}{#3}{#4}}}

\def\@spxthm#1#2#3#4{\@spbegintheorem{#2}{\csname the#1\endcsname}{#3}{#4}%
                    \ignorespaces}

\def\@spythm#1#2#3#4[#5]{\@spopargbegintheorem{#2}{\csname
       the#1\endcsname}{#5}{#3}{#4}\ignorespaces}

\def\@spbegintheorem#1#2#3#4{\trivlist
                 \item[\hskip\labelsep{#3#1\ #2\@thmcounterend}]#4}

\def\@spopargbegintheorem#1#2#3#4#5{\trivlist
      \item[\hskip\labelsep{#4#1\ #2}]{#4(#3)\@thmcounterend\ }#5}

% definition of \spnewtheorem* without number

\def\@sthm#1#2{\@Ynthm{#1}{#2}}

\def\@Ynthm#1#2#3#4{\expandafter\@ifdefinable\csname #1\endcsname
   {\global\@namedef{#1}{\@Thm{\csname #1name\endcsname}{#3}{#4}}%
    \expandafter\xdef\csname #1name\endcsname{#2}%
    \global\@namedef{end#1}{\@endtheorem}}}

\def\@Thm#1#2#3{\topsep 7\p@ \@plus2\p@ \@minus4\p@
\@ifnextchar[{\@Ythm{#1}{#2}{#3}}{\@Xthm{#1}{#2}{#3}}}

\def\@Xthm#1#2#3{\@Begintheorem{#1}{#2}{#3}\ignorespaces}

\def\@Ythm#1#2#3[#4]{\@Opargbegintheorem{#1}
       {#4}{#2}{#3}\ignorespaces}

\def\@Begintheorem#1#2#3{#3\trivlist
                           \item[\hskip\labelsep{#2#1\@thmcounterend}]}

\def\@Opargbegintheorem#1#2#3#4{#4\trivlist
      \item[\hskip\labelsep{#3#1}]{#3(#2)\@thmcounterend\ }}

\if@envcntsect
   \def\@thmcountersep{.}
   \spnewtheorem{theorem}{Theorem}[section]{\bfseries}{\itshape}
\else
   \spnewtheorem{theorem}{Theorem}{\bfseries}{\itshape}
   \if@envcntreset
      \@addtoreset{theorem}{section}
   \else
      \@addtoreset{theorem}{chapter}
   \fi
\fi

%definition of divers theorem environments
\spnewtheorem*{claim}{Claim}{\itshape}{\rmfamily}
\spnewtheorem*{proof}{Proof}{\itshape}{\rmfamily}
\if@envcntsame % alle Umgebungen wie Theorem.
   \def\spn@wtheorem#1#2#3#4{\@spothm{#1}[theorem]{#2}{#3}{#4}}
\else % alle Umgebungen mit eigenem Zaehler
   \if@envcntsect % mit section numeriert
      \def\spn@wtheorem#1#2#3#4{\@spxnthm{#1}{#2}[section]{#3}{#4}}
   \else % nicht mit section numeriert
      \if@envcntreset
         \def\spn@wtheorem#1#2#3#4{\@spynthm{#1}{#2}{#3}{#4}
                                   \@addtoreset{#1}{section}}
      \else
         \def\spn@wtheorem#1#2#3#4{\@spynthm{#1}{#2}{#3}{#4}
                                   \@addtoreset{#1}{chapter}}%
      \fi
   \fi
\fi
\spn@wtheorem{case}{Case}{\itshape}{\rmfamily}
\spn@wtheorem{conjecture}{Conjecture}{\itshape}{\rmfamily}
\spn@wtheorem{corollary}{Corollary}{\bfseries}{\itshape}
\spn@wtheorem{definition}{Definition}{\bfseries}{\itshape}
\spn@wtheorem{example}{Example}{\itshape}{\rmfamily}
\spn@wtheorem{exercise}{Exercise}{\itshape}{\rmfamily}
\spn@wtheorem{lemma}{Lemma}{\bfseries}{\itshape}
\spn@wtheorem{note}{Note}{\itshape}{\rmfamily}
\spn@wtheorem{problem}{Problem}{\itshape}{\rmfamily}
\spn@wtheorem{property}{Property}{\itshape}{\rmfamily}
\spn@wtheorem{proposition}{Proposition}{\bfseries}{\itshape}
\spn@wtheorem{question}{Question}{\itshape}{\rmfamily}
\spn@wtheorem{solution}{Solution}{\itshape}{\rmfamily}
\spn@wtheorem{remark}{Remark}{\itshape}{\rmfamily}

\def\@takefromreset#1#2{%
    \def\@tempa{#1}%
    \let\@tempd\@elt
    \def\@elt##1{%
        \def\@tempb{##1}%
        \ifx\@tempa\@tempb\else
            \@addtoreset{##1}{#2}%
        \fi}%
    \expandafter\expandafter\let\expandafter\@tempc\csname cl@#2\endcsname
    \expandafter\def\csname cl@#2\endcsname{}%
    \@tempc
    \let\@elt\@tempd}

\def\theopargself{\def\@spopargbegintheorem##1##2##3##4##5{\trivlist
      \item[\hskip\labelsep{##4##1\ ##2}]{##4##3\@thmcounterend\ }##5}
                  \def\@Opargbegintheorem##1##2##3##4{##4\trivlist
      \item[\hskip\labelsep{##3##1}]{##3##2\@thmcounterend\ }}
      }

% the abstract environment is re-written by Ming Kin Lai
% textwidth is 4.5 inches for final mode, 6.5 inches for 
% draft mode, so the minipage's width is 3.84 inches
% for final mode and 5.8 inches for draft mode 
% after the 0.66-inch indent
\renewenvironment{abstract}{%
   \if@draftmode%
     \def\abstractwidth{5.8in}
   \else
     \def\abstractwidth{3.84in}
     \footnotesizesv\smallersize
   \fi
   \bigskip\noindent\leavevmode\hspace*{0.66in}%
   \begin{minipage}\abstractwidth\hrule\par\medskip}{%
   \par\medskip\hrule\end{minipage}}%

%\newenvironment{abstract}{  
%  \list{}{\advance\topsep by0.35cm\relax\small  
%      {%
%     \leftmargin=1cm
%      \labelwidth=\z@
%      \listparindent=\z@
%      \itemindent\listparindent
%      \rightmargin\leftmargin
%      }%
%  \item[\hskip\labelsep\bfseries\abstractname]
%   \par   
%  }
%  {\endlist}

% keywords environment created by Ming Kin Lai
% see comments in the abstract environmnet
\newenvironment{keywords}{%   
   \if@draftmode
     \def\keywordwidth{5.8in}
   \else
     \def\keywordwidth{3.84in}
     \scriptsize\smallersize
   \fi
   \noindent\leavevmode\hspace*{0.66in}%
   \begin{minipage}\keywordwidth\medskip%
      \if@draftmode%
          \keywordname
      \else
          {\bfseries\textsf{\keywordname}}
      \fi
   }%
   {\end{minipage}}

\newdimen\headlineindent             % dimension for space between
\headlineindent=1.166cm              % number and text of headings.

\def\ps@headings{\let\@mkboth\@gobbletwo
   \let\@oddfoot\@empty\let\@evenfoot\@empty
%   \def\@evenhead{\normalfont\small\rlap{\thepage}\hspace{\headlineindent}%
%                  \leftmark\hfil}  % changed by Ming Kin Lai
   \if@draftmode%
     \def\@evenhead{\normalfont\small\rlap{\thepage}\hfil\leftmark}
   \else
     \def\@evenhead{\bfseries\textsf{\footnotesizesv\rlap{\thepage}}\hfil\leftmark}
   \fi  
%   \def\@oddhead{\normalfont\small\hfil\rightmark\hspace{\headlineindent}%
%                 \llap{\thepage}}
   \if@draftmode%
     \def\@oddhead{\normalfont\small\rightmark\hfil\llap{\thepage}}
   \else
     \def\@oddhead{\bfseries\footnotesizesv\rightmark\hfil\llap{\textsf{\thepage}}}
   \fi
   \def\chaptermark##1{}%
   \def\sectionmark##1{}%
   \def\subsectionmark##1{}}

\def\ps@titlepage{\let\@mkboth\@gobbletwo
% header and footer of the title page changed by Ming Kin Lai - begin
%   \let\@oddfoot\@empty\let\@evenfoot\@empty
%   \def\@evenhead{\normalfont\small\rlap{\thepage}\hspace{\headlineindent}%
%                  \hfil}
%   \def\@oddhead{\normalfont\small\hfil\hspace{\headlineindent}%
%                 \llap{\thepage}}
   \if@draftmode%
     \def\@oddfoot{\normalfont\small\centerline{\thepage}}
   \else
     \def\@oddfoot{\bfseries\footnotesizesv\centerline{\textsf{\thepage}}}
   \fi
   \let\@evenfoot\@oddfoot
   \if@draftmode% 
     \let\@oddhead\@empty\let\@evenhead\@empty
   \else
%     \def\@evenhead{\scriptsize\rlap{\textit{\titlepageheader}}%
     \def\@evenhead{\smallersize%
                    \rlap{\textit{\titlepageheader}}%
                  \hspace{\headlineindent}\hfil}
     \let\@oddhead\@evenhead
   \fi
% header and footer of the title page changed by Ming Kin Lai - end
   \def\chaptermark##1{}%
   \def\sectionmark##1{}%
   \def\subsectionmark##1{}}

\if@runhead\ps@headings\else
\ps@empty\fi

% final mode for footnote added by Ming Kin Lai - begin
\if@draftmode
\else
  \let\oldfootnote\footnote
  \renewcommand\footnote[1]{\oldfootnote{%
    \linespread{1}\smallersize#1}}
\fi
% final mode for footnote added by Ming Kin Lai - end

\setlength\arraycolsep{1.4\p@}
\setlength\tabcolsep{1.4\p@}

\endinput
%end of file llncs.cls
