package slimeknights.tconstruct.library.tools;

import net.minecraftforge.common.util.Lazy;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.AbstractToolStatsBuilder;
import slimeknights.tconstruct.tools.ToolStatsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The data defining a tinkers tool, e.g. a pickaxe or a hammer.
 * Note that this defines the tool metadata itself, not an instance of the tool.
 * Contains information about what's needed to craft the tool, how it behaves...
 */
public class ToolDefinition {
  public static final ToolDefinition EMPTY = new ToolDefinition(new ToolBaseStatDefinition.Builder().build(), Collections::emptyList);
  /** Default stat builder for melee and harvest tools */
  public static final BiFunction<ToolDefinition,List<IMaterial>,? extends AbstractToolStatsBuilder> TOOL_STAT_BUILDER = ToolStatsBuilder::from;

  /** Inherent stats of the tool. */
  private final ToolBaseStatDefinition baseStatDefinition;
  /** The tool parts required to build this tool. */
  protected final Lazy<List<IToolPart>> requiredComponents;
  /** Modifiers applied automatically by this tool */
  protected final Lazy<List<ModifierEntry>> modifiers;
  /** Function to convert from tool definition and materials into tool stats */
  protected final BiFunction<ToolDefinition,List<IMaterial>,? extends AbstractToolStatsBuilder> statsBuilder;

  /** Cached indices that can be used to repair this tool */
  private int[] repairIndices;

  public ToolDefinition(ToolBaseStatDefinition baseStatDefinition, Supplier<List<IToolPart>> requiredComponents, Supplier<List<ModifierEntry>> modifiers, BiFunction<ToolDefinition,List<IMaterial>,? extends AbstractToolStatsBuilder> statsBuilder) {
    this.baseStatDefinition = baseStatDefinition;
    this.requiredComponents = Lazy.of(requiredComponents);
    this.modifiers = Lazy.of(modifiers);
    this.statsBuilder = statsBuilder;
  }

  public ToolDefinition(ToolBaseStatDefinition baseStatDefinition, Supplier<List<IToolPart>> requiredComponents, Supplier<List<ModifierEntry>> modifiers) {
    this(baseStatDefinition, requiredComponents, modifiers, TOOL_STAT_BUILDER);
  }

  public ToolDefinition(ToolBaseStatDefinition baseStatDefinition, Supplier<List<IToolPart>> requiredComponents) {
    this(baseStatDefinition, requiredComponents, Collections::emptyList);
  }

  /**
   * Gets the current tools base stats definition
   *
   * @return the tools base stats definition
   */
  public ToolBaseStatDefinition getBaseStatDefinition() {
    return this.baseStatDefinition;
  }

  /**
   * Gets the required components for the given tool definition
   * @return the required components
   */
  public List<IToolPart> getRequiredComponents() {
    return this.requiredComponents.get();
  }

  /**
   * Builds the stats for this tool definition
   * @param materials  Materials list
   * @return  Stats NBT
   */
  public StatsNBT buildStats(List<IMaterial> materials) {
    return statsBuilder.apply(this, materials).buildStats();
  }

  /** Gets the modifiers applied by this tool */
  public List<ModifierEntry> getModifiers() {
    return modifiers.get();
  }

  /* Repairing */

  /** Returns a list of part material requirements for repair materials */
  public int[] getRepairParts() {
    if (repairIndices == null) {
      // get indices of all head parts
      List<IToolPart> components = requiredComponents.get();
      repairIndices = IntStream.range(0, components.size())
                               .filter(i -> MaterialRegistry.getInstance().getDefaultStats(components.get(i).getStatType()) instanceof IRepairableMaterialStats)
                               .toArray();
    }
    return repairIndices;
  }
}
