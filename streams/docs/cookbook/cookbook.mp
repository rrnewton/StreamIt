%
% cookbook.mp: MetaPost diagrams for StreamIt Cookbook
% David Maze <dmaze@cag.lcs.mit.edu>
% $Id: cookbook.mp,v 1.9 2003-10-13 16:08:04 thies Exp $
%
% MetaPost is a drawing language based on Knuth's METAFONT that can
% produce embedded PostScript figures, with TeX strings embedded in it.
% It produces EPS files named cookbook.1, cookbook.2, ..., cookbook.n
% if you run 'mpost cookbook'.  Run 'texdoc mpintro' or 'texdoc mpman'
% for more infomration.
%

% Settings:
defaultfont := "rphvr";
defaultscale := 11pt/fontsize defaultfont;

% These macros probably want to be broken out into separate code, but...

% Basic unit length:
u=11pt;
% Border around text in filters
filterBorder=3pt;
% Border between pipeline label and contents
pipeBorder=5pt;
% Standard block width:
fw=5u;

% Usage: height(p), width(p)
% Returns the height or width of the bounding box of p.
def height expr p = ypart (ulcorner bbox p - llcorner bbox p) enddef;
def width expr p = xpart (urcorner bbox p - ulcorner bbox p) enddef;
def ytop expr p = ypart ulcorner bbox p enddef;
def ybot expr p = ypart llcorner bbox p enddef;
def xleft expr p = xpart llcorner bbox p enddef;
def xright expr p = xpart lrcorner bbox p enddef;

% Draw an arrowed line into a picture.
def arrowline (expr p, pa) = addto p doublepath pa withpen currentpen;
  addto p contour arrowhead pa withpen currentpen; enddef;

% Returns p, but with name placed at its top left and a box around the
% whole thing.  Add ot distance on top and ob distance below.
vardef boxed(expr name, ot, ob)(suffix p) =
  addto p doublepath (ulcorner p+(-u,ot))--(urcorner p+(u,ot))--
    (lrcorner p+(u,-ob))--(llcorner p+(-u,-ob))--cycle withpen currentpen;
  addto p also thelabel.lrt(name, ulcorner p);
  p
enddef;

% Usage: filter("name")
% Returns a picture expression.
vardef filter(expr name) =
  save p; picture p; p=nullpicture;
  addto p doublepath (0,-filterBorder)--(0,u+filterBorder)--(fw,u+filterBorder)--(fw,-filterBorder)--cycle withpen currentpen;
  addto p also thelabel(name, (fw/2,u/2));
  p
enddef;

% Usage:
%  draw toppipe("program", filter("foo"), filter("bar"));
% Returns a picture expression, drawing a pipeline without entry or
% exit arrows.
vardef toppipe(expr name)(text t) =
  save _h, _p, _q, _r;
  picture _p; _p=nullpicture;
  _h := 0;
  % What's actually going to get returned is a box with its top center at
  % 0,0.
  for v=t:
    picture _q; _q=v;
    if _h <> 0:
      addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
      addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
      _h := _h - u;
    fi
    picture _r; _r = _q shifted (-(width _q)/2-xleft _q,_h-ytop _q);
    addto _p also _r;
    _h := _h - height _r;
  endfor;
  boxed(name, u+pipeBorder, u, _p)
enddef;
  
% Usage:
%  draw pipeline("pipe", filter("foo"), filter("bar"));
% Returns a picture expression.
vardef pipeline(expr name)(text t) =
  save _h, _p, _q, _r;
  picture _p; _p=nullpicture;
  _h := 0;
  % What's actually going to get returned is a box with its top center at
  % 0,0.
  for v=t:
    picture _q; _q=v;
    addto _p contour makepath currentpen shifted (0,_h);
    addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
    addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
    _h := _h - u;
    picture _r; _r = _q shifted (-(width _q)/2-xleft _q,_h-ytop _q);
    addto _p also _r;
    _h := _h - height _r;
  endfor;
  addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
  addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
  _h := _h-u;
  % leave off border for anonymous pipelines
  if name="":
    boxed(name, 0, 0, _p)
  else:
    boxed(name, pipeBorder, 0, _p)
  fi
enddef;

% Usage:
%  draw splitjoin("sj", "dup", "rr")
%    (filter("foo"), filter("bar"))("","")("1","2")
% ...where the parameters are name, split type, join type; children;
% splitter weights; joiner weights.  Returns a picture expression.
vardef splitjoin(expr name, split, join)(text children)(text sws)(text jws) =
  save _p, _h, _x, _i, _n, _w, _q, _r, _y, _xc, _path;
  picture _p; _p=nullpicture;
  path _path;
  % Draw each of the children.
  _i := 0; _w := 0;
  for v=children:
    _i := _i + 1;
    picture _q; _q=v;
    % Goal: left side at _w, vertical center at 0.
    picture _r; _r = _q shifted (_w-(xleft _q),-(ybot _q)-(height _q)/2);
    addto _p also _r;
    _h[_i] := (height _q) / 2;
    _x[_i] := _w + (width _q) / 2;
    _w := _w + (width _q) + u;
  endfor;
  _n := _i;
  % Draw arrows for splitter/joiner.
  _xc := ((xleft _p) + (xright _p)) / 2;
  _y := (ytop _p) + u;
  _i := 0;
  for v=sws:
    _i := _i + 1;
    exitif _i > _n;
    _path := (_xc,_y)--(_x[_i],_h[_i]);
    % NB: this assumes the linear path above.
    addto _p also thelabel.top(v, .3[(_x[_i],_h[_i]),(_xc,_y)]);
    addto _p doublepath _path withpen currentpen;
    addto _p contour arrowhead _path withpen currentpen;
  endfor;
  % Splitter, and entry arrow:
  addto _p also thelabel.top(split, (_xc,_y));
  addto _p doublepath (_xc,_y+2u)--(_xc,_y+u) withpen currentpen;
  addto _p contour arrowhead (_xc,_y+2u)--(_xc,_y+u) withpen currentpen;
  _y := (ybot _p) - u;
  _i := 0;
  for v=jws:
    _i := _i + 1;
    exitif _i > _n;
    _path := (_x[_i],-_h[_i])--(_xc,_y);
    % NB: this assumes the linear path above.
    addto _p also thelabel.bot(v, .3[(_x[_i],-_h[_i]),(_xc,_y)]);
    addto _p doublepath _path withpen currentpen;
    addto _p contour arrowhead _path withpen currentpen;
  endfor;
  % Joiner, and exit arrow:
  addto _p also thelabel.bot(join, (_xc,_y));
  addto _p doublepath (_xc,_y-u)--(_xc,_y-2u) withpen currentpen;
  addto _p contour arrowhead (_xc,_y-u)--(_xc,_y-2u) withpen currentpen;
  boxed(name, 0, 0, _p)
enddef;

% Usage:
%  draw feedbackloop("feedback", "RR", "DUP",
%    filter("foo"), filter("bar"), "1","1", "","")
% ...where the parameters are name, join type, split type; children;
% joiner weights; splitter weights.  Returns a picture expression.
vardef feedbackloop(expr name, join, split, cb, cl, jx, jl, sx, sl) =
  save _p, _q, _r, _path, _xb, _xl, _hb, _hl, _y, _label;
  picture _p, _q, _r, _label; _p=nullpicture;
  path _path;
  % Draw the children.
  _q := cb;
  _r := _q shifted (-u/2-xright _q,-(ybot _q)-(height _q)/2);
  _xb := -u/2-(width _q)/2;
  _hb := (height _q)/2;
  addto _p also _r;
  _q := cl;
  _r := _q shifted (u/2-xleft _q,(-ybot _q)-(height _q)/2);
  _xl := u/2+(width _q)/2;
  _hl := (height _q)/2;
  addto _p also _r;
  % Okay.  Joiner:
  _y := (ytop _p) + u;
  _label := thelabel(join, (0,_y));
  addto _p also _label;
  addto _p doublepath (xleft _label,_y)--(_xb,_y)--(_xb,_hb)
  withpen currentpen;
  addto _p contour arrowhead (_xb,_y)--(_xb,_hb) withpen currentpen;
  addto _p doublepath (_xl,_hl)--(_xl,_y)--(xright _label,_y)
  withpen currentpen;
  addto _p contour arrowhead (_xl,_y)--(xright _label,_y) withpen currentpen;
  addto _p also thelabel.top(jl, ((_xl+xright _label)/2,_y));
  _y := (ytop _label);
  addto _p doublepath (0,_y+u)--(0,_y) withpen currentpen;
  addto _p contour arrowhead (0,_y+u)--(0,_y) withpen currentpen;
  addto _p also thelabel.rt(jx, (0,_y+u/2));
  % And splitter:
  _y := (ybot _p) - u;
  _label := thelabel(split, (0,_y));
  addto _p also _label;
  addto _p doublepath (_xb,-_hb)--(_xb,_y)--(xleft _label,_y)
  withpen currentpen;
  addto _p contour arrowhead (_xb,_y)--(xleft _label,_y) withpen currentpen;
  addto _p doublepath (xright _label,_y)--(_xl,_y)--(_xl,-_hl)
  withpen currentpen;
  addto _p contour arrowhead (_xl,_y)--(_xl,-_hl) withpen currentpen;
  addto _p also thelabel.bot(sl, ((_xl+xright _label)/2,_y));
  _y := (ybot _label);
  addto _p doublepath (0,_y)--(0,_y-u) withpen currentpen;
  addto _p contour arrowhead (0,_y)--(0,_y-u) withpen currentpen;
  addto _p also thelabel.rt(sx, (0,_y-u/2));
  boxed(name, 0, 0, _p)
enddef;

% End of macro section.

beginfig(0);
  draw toppipe("FMRadio",
    filter("LowPass"),
    filter("Demod"),
    splitjoin("Equalizer","DUP","RR")
    (filter("BandPass"),thelabel("...",(0,0)),filter("BandPass"))
    ("","","")("1","","1"),
    filter("Adder"));
endfig;

beginfig(1);
  draw toppipe("Minimal",
    filter("IntSource"),
    filter("IntPrinter"));
endfig;

beginfig(2);
  draw toppipe("MovingAverage",
    filter("IntSource"),
    filter("Averager"),
    filter("IntPrinter"));
endfig;

beginfig(3);
  draw pipeline("BandPassFilter",
    splitjoin("BPFCore", "DUP", "RR")
    (filter("LowPass"),filter("LowPass"))("","")("1","1"),
    filter("Subtracter"));
endfig;

beginfig(4);
  picture c;
  c := pipeline("", filter("BandPass"), filter("Scale"));
  draw pipeline("Equalizer",
    splitjoin("EqSplit", "DUP", "RR")
    (c, thelabel("...", (0,0)), c)("","","")("1","","1"),
    filter("+"));
endfig;

beginfig(5);
  draw feedbackloop("Echo", "RR", "RR")(filter("AddBypass"),filter("Scale"))
  ("1","1")("","");
endfig;

end
