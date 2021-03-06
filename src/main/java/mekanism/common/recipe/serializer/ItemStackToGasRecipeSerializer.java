package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import net.minecraft.network.PacketBuffer;

public class ItemStackToGasRecipeSerializer<T extends ItemStackToGasRecipe> extends ItemStackToChemicalRecipeSerializer<Gas, GasStack, T> {

    public ItemStackToGasRecipeSerializer(IFactory<Gas, GasStack, T> factory) {
        super(factory);
    }

    @Override
    protected GasStack fromJson(@Nonnull JsonObject json, @Nonnull String key) {
        return SerializerHelper.getGasStack(json, key);
    }

    @Override
    protected GasStack fromBuffer(@Nonnull PacketBuffer buffer) {
        return GasStack.readFromPacket(buffer);
    }
}