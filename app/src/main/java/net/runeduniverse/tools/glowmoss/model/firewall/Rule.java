package net.runeduniverse.tools.glowmoss.model.firewall;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;

@Getter
@NodeEntity(label = "RULE")
@Accessors(chain = true)
public class Rule {

	private static final String REL_LABEL_JUMP = "JUMP";
	private static final String REL_LABEL_GOTO = "GOTO";

	@Setter(onMethod_ = { @Deprecated })
	@Relationship(label = "NEXT", direction = Direction.OUTGOING)
	private Rule next;

	@Setter
	@Relationship(label = REL_LABEL_JUMP, direction = Direction.BIDIRECTIONAL)
	private Chain jumpTo = null;

	@Setter
	@Relationship(label = REL_LABEL_GOTO, direction = Direction.OUTGOING)
	private Chain goTo = null;

	@Setter
	private String content;

}
