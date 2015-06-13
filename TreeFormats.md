Standard format for transmission trees.

# Introduction #

# Details #

Erik Volz <erikvolz@umich.edu> 	Sat, Nov 14, 2009 at 10:21 PM
To: Shah Jamal Alam <sjalam@umich.edu>
Cc: Jong-Hoon Kim <kimfinale@gmail.com>, ethan.obie.rs@gmail.com
Ok-- it looks like the thing to do is to go with GraphML. We will need the following extra attributes in each graph:

<key id="d0" for="node" attr.name="infector" attr.type="integer" />
<key id="d1" for="node" attr.name="infected" attr.type="integer" />
<key id="d2" for="edge" attr.name="branchLength" attr.type="double"/>

And then for every declaration of a node, you would have a subfield:



&lt;node id="x"&gt;




&lt;data key="d0"&gt;

1

&lt;/data&gt;




&lt;data key="d1"&gt;

2

&lt;/data&gt;




&lt;/node&gt;



which in this case says that node "x" corresponds to a transmission event by node 1 to node 2.

For a primer on the GraphML format, see here
http://graphml.graphdrawing.org/primer/graphml-primer.html

Erik

On Thu, Nov 12, 2009 at 11:27 AM, Shah Jamal Alam <sjalam@umich.edu> wrote:

> Thanks guys,

> GraphML seems a good choice to me- Will have a look at it in detail and will let you guys know about any further thoughts.

> Could I have just the recent Java/Python projects (a list would do for now) that we need to put in the code repository of the lab?

> thanks,
> jamal

> Jong-Hoon Kim wrote:


> Thanks the note, guys.

> I have yet to take a look at GraphML or Nexus or Newick although I have some experience with python (networkx) and java (jung) . Whichever you choose should be fine with me.

> Jong-Hoon



> 2009/11/11 <ethan.obie.rs <http://ethan.obie.rs>@gmail.com <http://gmail.com>>


> I'm OK with using GraphML. JUNG also has readers/writers for
> GraphML (I think), so it might be a nice go-between for java and
> python apps.

> On Nov 11, 2009 6:25pm, Erik Volz <erikvolz@umich.edu
> <[mailto:erikvolz@umich.edu>>](mailto:erikvolz@umich.edu>>) wrote:
> > Some kind of XML format could be a good choice, since there are
> many parsers/writers already available. The networkx library in
> python seems to have parsers for GraphML, which would be very
> handy for me. We would not need to add too many data fields beyond
> what is in the default GraphML specification-- just branch lengths
> and node-id's for the infector and infected. Thoughts ?
> >
> >
> >
> > Erik
> >
> > On Wed, Nov 11, 2009 at 4:14 PM, Ethan Romero-Severson
> ethan.obie.rs <http://ethan.obie.rs>@gmail.com <http://gmail.com>>

> wrote:
> >
> >
> > Concerning a common file format for representing trees, I see the
> >
> > following options
> >
> >
> >
> > 1) Newick
> >
> > 2) Nexus
> >
> > 3) GraphML
> >
> > 4) Pajek
> >
> > 5) In-house custom format
> >
> >
> >
> > So, the advantage of Newick and Nexus formats are that they are
> light
> >
> > weight. We could easily represent even very large trees efficiently
> >
> > (in terms of storage). However, as far as I can tell,
> associating data
> >
> > beyond a simple edge weight is not supported in either format.
> GraphML
> >
> > is based on XML and can there for be extended to include arbitrary
> >
> > data fields associated with nodes/edges. Also schema could be
> designed
> >
> > using GraphML that would enforce well-formating, that is, we could
> >
> > test to see if a GraphML file had all of the correct info for
> >
> > whichever application it was using. Pajek format is not wildly used
> >
> > (except in Pajek itself of course) but it is very simple and could
> >
> > easily be extended to included multiple data fields. Finally, the
> >
> > advantage of a custom format is that we can define it in a way to
> >
> > maximize the efficiency of both the algorithms that run on it
> and the
> >
> > amount of disk space it takes up. However, any custom file would be
> >
> > unique to our group and we would have to write custom parsers if we
> >
> > wanted to interface with 3rd party software.
> >
> >
> >
> > I'm not ready to advocate for one option, but I will say that the
> >
> > function Z(.) that Erik and I were discussing--the one that traces
> >
> > back to the root the internal nodes in the phylogeny--which makes
> >
> > calculation of the branching times quite simple would be most
> easy to
> >
> > calculate using a custom format. The most computationally efficient
> >
> > format would basically  be a listing for every sampled tip of Z(.)
> >
> > where each value included some kind of unique ID and a time. A
> >
> > phylogeny could be reconstructed from such data, and calculation of
> >
> > Z(.) is already done. However this would be a large file. An
> >
> > alternative format might record each transmission along a
> lineage. For
> >
> > example:
> >
> >
> >
> > uid=1, data.field1=?, data.field2=?, ... > uid=50, data.field1=?,
> >
> > data.field2=?, ... > uid=100, data.field1=?, data.field2=?, ... >
> >
> > .
> >
> > .
> >
> > .
> >
> >
> >
> > Where each newline represents a single infected agent's lineage, by
> >
> > which I mean the internal node that represents the branching point
> >
> > that begins their lineage (assuming a non-root node) proceeding
> >
> > through to their removal or sampling. This format would be much more
> >
> > compact but would require significant "reconstitution" to get out
> >
> > meaningful information.
> >
> > -e
> >
> >
> >
> >
> >
> >
> >
> > --
> >
> > Ethan Romero-Severson
> >
> > Ph.D. Candidate
> >
> > Dept. of Epidemiology
> >
> > University of Michigan
> >
> > -----
> >
> > "It's like 'Gay Survivor,' we're going to outlive, outlast, and
> >
> > outsmart the bigots." -- Dan Savage
> >
> >
> >
> >
> > --
> > Erik M Volz
> >
> > Department of Epidemiology
> > University of Michigan- Ann Arbor
> > 1415 Washington Heights
> > Ann Arbor, MI 48109-2029
> > erikvolz@umich.edu <[mailto:erikvolz@umich.edu>](mailto:erikvolz@umich.edu>)

> >
> >
> > http://www.erikvolz.info
> >




> --
> Jong-Hoon Kim
> +1 734 764 4470 (office)
> +1 734 657 8127 (cell)



> --
> Shah Jamal Alam
> Dept. of Epidemiology, School of Public Health,
> University of Michigan, Ann Arbor MI 48109
> E-mail: sjalam@umich.edu  Ph: 734-763-1393
> http://www.umich.edu/~sjalam




--
Erik M Volz

Department of Epidemiology
University of Michigan- Ann Arbor
1415 Washington Heights
Ann Arbor, MI 48109-2029
erikvolz@umich.edu
http://www.erikvolz.info