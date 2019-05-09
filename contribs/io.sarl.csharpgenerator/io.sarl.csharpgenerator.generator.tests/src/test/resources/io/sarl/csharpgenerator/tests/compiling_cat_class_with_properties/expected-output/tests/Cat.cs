namespace tests
{
	public class Cat
	{
		public string name { get; private set; }
		public int bellyCapacity { get; private set; }
		public int bellyContent { get; set; }
		public boolean isGrumpy { get; set; }

		public Cat(string name, int bellyCapacity)
		{
			this.name = name;
			this.bellyCapacity = bellyCapacity;
			this.bellyContent = bellyCapacity / 2;
			this.isGrumpy = false;
		}
	}
}
