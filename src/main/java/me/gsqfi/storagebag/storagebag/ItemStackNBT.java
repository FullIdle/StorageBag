package me.gsqfi.storagebag.storagebag;

import lombok.SneakyThrows;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ItemStackNBT {
    private final ItemStack original;
    private final Object nbt;

    public ItemStackNBT(ItemStack original) {
        this.original = original;
        this.nbt = getNBT(this.original.getItemMeta());
    }

    @SneakyThrows
    public ItemStack getNnbtFormatItemStack(){
        ItemMeta clone = this.original.getItemMeta().clone();
        NBTCom.internalTag.set(clone,this.nbt);
        ItemStack clone1 = this.original.clone();
        clone1.setItemMeta(clone);
        return clone1;
    }

    @SneakyThrows
    private Object getNBT(ItemMeta itemMeta) {
        Object o = NBTCom.internalTag.get(itemMeta);
        return o == null?NBTCom.newNBTTagCompound():o;
    }

    @SneakyThrows
    public String getString(String path){
        return (String) NBTCom.getStringMethod.invoke(this.nbt,path);
    }

    @SneakyThrows
    public void setString(String path,String value){
        NBTCom.setStringMethod.invoke(this.nbt,path,value);
    }


    @SneakyThrows
    public static void init() {
        ItemMeta itemMeta = new ItemStack(Material.GRASS).getItemMeta();
        {
            NBTCom.metaItemClas = itemMeta.getClass();
            NBTCom.internalTag = NBTCom.metaItemClas.getDeclaredField("internalTag");
            NBTCom.internalTag.setAccessible(true);
        }
        {
            /*nbtTag,nbtBase*/
            NBTCom.nbtTagCompoundClas = NBTCom.internalTag.getType();
            NBTCom.nbtBaseClas = NBTCom.nbtTagCompoundClas.getInterfaces()[0];
        }
        {
            NBTTagCompound d = new NBTTagCompound();
            //Method
            for (Method method : NBTCom.nbtTagCompoundClas.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    //setString
                    if (method.getReturnType().equals(Void.TYPE)&&
                            parameterTypes.length == 2 &&
                            parameterTypes[0].equals(String.class) &&
                            parameterTypes[1].equals(String.class)
                    ) {
                        NBTCom.setStringMethod = method;
                        NBTCom.setStringMethod.setAccessible(true);
                        continue;
                    }
                    //getString
                    if (method.getReturnType().equals(String.class)&&
                            parameterTypes.length == 1&&
                            parameterTypes[0].equals(String.class)
                    ){
                        NBTCom.getStringMethod = method;
                        NBTCom.getStringMethod.setAccessible(true);
                        continue;
                    }
                }
            }
        }
    }

    private static class NBTCom {
        public static Class<? extends ItemMeta> metaItemClas;
        public static Class<?> nbtTagCompoundClas;
        public static Class<?> nbtBaseClas;

        public static Field internalTag;

        public static Method setStringMethod;
        public static Method getStringMethod;

        @SneakyThrows
        public static Object newNBTTagCompound() {
            Constructor<?> constructor = nbtTagCompoundClas.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }
}
